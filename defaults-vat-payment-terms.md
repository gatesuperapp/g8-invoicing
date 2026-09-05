# Défauts : taux de TVA, mention d'exonération, termes de paiement

Trois systèmes distincts avec des règles de résolution différentes. Récap
compact pour retrouver le "pourquoi ça marche comme ça".

## 1. Taux de TVA seedés dans le picker Produits

**Table** : `TaxRate` (`shared/src/commonMain/sqldelight/g8invoicing/TaxRate.sq`)

**Défaut hardcodé (fresh install sans onboarding country)** : `5.5 / 10 / 20`
(taux français), inséré par le `INSERT` bake dans le `.sq` — se déclenche à la
création de la DB.

**Fresh install avec pays choisi à l'onboarding** : la shortlist des ~2-3 taux
les plus courants du pays remplace les défauts FR, via
`ProductTaxLocalDataSource.seedDefaultsForCountryIfPristine(countryCode)`
appelé depuis `FirstLaunchIssuerNameDialog.onSubmit`.

Mapping pays → taux : `CountryCodes.defaultVatRatesForCountry(code)`
(`shared/src/commonMain/kotlin/com/a4a/g8invoicing/data/models/CountryCodes.kt`).
~35 pays couverts (EU + CH/NO/IS/GB + AU/NZ/JP/CA + Andorre/Monaco). Pays hors
liste (dont **US** — pas de TVA fédérale) tombent sur les défauts FR.

**Garde de sécurité (`IfPristine`)** : la méthode ne remplace que si la table
contient toujours *exactement* `[5.5, 10, 20]`. Dès qu'un rate a été ajouté,
modifié ou supprimé, l'utilisateur a pris la main et on ne touche plus rien.

**Changement de pays émetteur en cours de vie** : aucun effet automatique sur
`TaxRate`. La table est globale (partagée par tous les émetteurs / entreprises),
pas indexée sur le pays. L'utilisateur ajuste manuellement depuis le picker si
besoin.

**Restauration d'une sauvegarde** : la table `TaxRate` est restaurée telle
qu'elle était dans le backup. Pas de re-seed pays.

## 2. Mention d'exonération de TVA (BT-120)

**Colonne** : `vat_exemption_text` sur `Invoice` et `CreditNote` (schéma
5.sqm). Uniquement sur ces deux types de docs — BL et devis n'ont pas ce
champ.

**Indexation** : sur le **pays de l'émetteur**, pas la langue. Un
`§ 19 UStG` est une citation légale allemande, elle reste en allemand même
sur une facture affichée en anglais.

**Résolveur 3 layers** :
`resolveVatExemptionText(country)` dans `data/models/VatExemption.kt` :

1. **Clé nationale** si dispo — `vat_exempt_<CC>` dans `strings.xml`.
   Actuellement : `FR` (+ `MC`, union fiscale), `DE`, `IT`. Native language
   dans les 4 locales (pas paraphrasé).
2. **Fallback UE** — `vat_exempt_fallback_eu` = art. 284 directive 2006/112/CE.
   Traduit par locale. Sert pour les 24 autres pays UE.
3. **Fallback générique** — `vat_exempt_fallback_generic` = "Non assujetti à
   la TVA". Traduit par locale. Sert pour tout le reste (GB, CH/LI, Andorre,
   Afrique, Asie, Amérique).

**Compose Multiplatform ne supporte pas `Res.string.byName(...)` dynamique** →
le résolveur est un gros `when` explicite. Ajouter un pays = 1 nouvelle clé
`vat_exempt_<CC>` sur la branche `translations` + 1 case dans le `when`.

**Reprise du doc précédent** :
`resolveVatExemptionForNewDoc(issuer, previousVatText, previousIssuerCountry)`
(même fichier).

Règle : on reprend la mention du **doc précédent du même type** pour le même
émetteur maître UNIQUEMENT si **le pays émetteur figé du doc précédent ==
pays émetteur courant**. Sinon on retombe sur le résolveur (défaut du pays
courant).

Rationale : le pays émetteur maître peut changer entre deux docs
(déménagement, changement de siège). Dans ce cas la mention du doc précédent
référence un régime légal qui n'est plus le bon.

**Cas US** : le résolveur renvoie `vat_exempt_fallback_generic` ("Not registered
for VAT") si un émetteur US toggle `vatExempt=true`. En pratique un émetteur US
n'a pas de raison d'activer cette toggle (pas de concept de TVA aux US) — s'il
le fait, la mention est légèrement inexacte mais surchargeable.

**Toujours surchargeable** : le champ reste éditable via le menu texte du doc.
Même dans un pays donné plusieurs régimes coexistent (franchise en base,
exonération d'activité, exonération art. 261…) et l'app ne devine pas lequel
s'applique. Le défaut = commodité, pas vérité.

## 3. Termes de paiement (escompte / pénalités de retard / frais de recouvrement)

**Colonnes** : `payment_terms_recovery_fees`, `payment_terms_late_fees`,
`payment_terms_discount` sur `Invoice` uniquement. Ni CN, ni BL, ni devis.

**Indexation** : sur la **langue de l'application**, pas le pays. C'est un
texte informatif adressé au client, il doit être dans la langue du doc pas
dans un régime légal spécifique.

**Défaut par langue** : clés `payment_terms_recovery_fees_default`,
`payment_terms_late_fees_default`, `payment_terms_discount_default` dans
`strings.xml`, traduites FR / EN / DE / ES.

**Reprise du doc précédent** : gardée par la langue.

Le `format_locale` du doc est figé à la création (via
`AppLocaleHolder.languageCode`). À la création d'un nouveau doc :

- Si `previous.format_locale == AppLocaleHolder.languageCode` → on reprend les
  3 champs du précédent (per-issuer via
  `getLastInvoicePaymentReuseForIssuer`, ou globalement via
  `getLastInsertedInvoicePaymentTerms`).
- Sinon → on prend les défauts localisés via `getString(Res.string.payment_terms_*_default)`
  qui respecte automatiquement la locale courante.

Rationale : si l'utilisateur passe de FR à EN puis crée une nouvelle facture,
il ne doit PAS hériter du wording FR sur son doc EN.

## 4. Moyens de paiement (label prefix + label bank)

**Colonnes** : `payment_means_label` (texte libre que l'user peut mettre
devant la liste des modes) et `payment_bank_label` (texte devant les coords
bancaires) sur `Invoice`.

Même garde de langue que les termes de paiement : à la création d'un nouveau
doc, la reprise du label du doc précédent n'a lieu que si
`previous.format_locale == AppLocaleHolder.languageCode`. Sinon → défauts
localisés via `document_payment_means_default_label` (traduit FR/EN/DE/ES).

**Attention** : ne s'applique QUE aux labels prose. Les autres champs
payment_means ne sont pas concernés :
- `payment_means_selections` (chip IDs TRANSFER/CHEQUE/CASH/…) : reprise
  inconditionnelle, ce sont des identifiants stables.
- `payment_means_other_checked` : boolean, reprise inconditionnelle.

**Changement de langue en cours de session** : la locale est captée à chaque
création via `AppLocaleHolder.languageCode`. Docs existants gardent leur
`format_locale` figé, nouveaux docs prennent la locale courante.

## Fichiers clés

| Concern | Fichier |
|---|---|
| Taux TVA seed pays | `data/models/CountryCodes.kt` (`defaultVatRatesForCountry`) |
| Taux TVA seed impl | `data/ProductTaxLocalDataSource.kt` (`seedDefaultsForCountryIfPristine`) |
| Taux TVA défaut DB | `sqldelight/g8invoicing/TaxRate.sq` (INSERT bake) |
| Mention TVA résolveur | `data/models/VatExemption.kt` |
| Mention TVA reprise/seed doc | idem, `resolveVatExemptionForNewDoc` |
| Mention TVA strings | `composeResources/values*/strings.xml` — clés `vat_exempt_*` |
| Payment terms reprise | `data/InvoiceLocalDataSource.kt` (`seedPaymentTermsForNewInvoice` + createNew) |
| Payment terms query | `sqldelight/g8invoicing/Invoice.sq` (`getLastInvoicePaymentReuseForIssuer`, `getLastInsertedInvoicePaymentTerms`) |
| Payment terms strings | `composeResources/values*/strings.xml` — clés `payment_terms_*_default` |
