# Documentation des Tests - G8 Invoicing

Ce fichier documente tous les tests automatisés de l'application.

**Commande pour lancer les tests :**
```bash
./gradlew :shared:testDebugUnitTest
```

---

## 1. Tests Utilitaires

### 1.1 PriceCalculationsTest (8 tests)
Tests des calculs de prix HT ↔ TTC.

| Test | Action utilisateur simulée |
|------|---------------------------|
| `calculatePriceWithTax_20percent` | Calcul TTC avec TVA 20% |
| `calculatePriceWithTax_5_5percent` | Calcul TTC avec TVA 5.5% |
| `calculatePriceWithTax_zero` | Calcul TTC sans TVA |
| `calculatePriceWithTax_withDecimals` | Calcul TTC avec décimales (arrondi) |
| `calculatePriceWithoutTax_20percent` | Calcul HT depuis TTC 20% |
| `calculatePriceWithoutTax_5_5percent` | Calcul HT depuis TTC 5.5% |
| `calculatePriceWithoutTax_zero` | Calcul HT sans TVA |
| `priceConversion_roundTrip` | Vérification HT→TTC→HT cohérent |

### 1.2 DateUtilsTest (17 tests)
Tests des fonctions de date.

| Test | Action utilisateur simulée |
|------|---------------------------|
| `formatDate_singleDigitDayAndMonth` | Affichage date 05/01/2024 |
| `formatDate_doubleDigitDayAndMonth` | Affichage date 25/12/2024 |
| `parseDate_validFormat` | Lecture date saisie utilisateur |
| `parseDate_withPaddedZeros` | Lecture date avec zéros |
| `parseDate_emptyString` | Champ date vide |
| `parseDate_blankString` | Champ date espaces |
| `parseDate_invalidFormat` | Date format incorrect |
| `parseDate_invalidDate` | Date impossible (32/13) |
| `parseDate_nonNumeric` | Texte au lieu de date |
| `getCurrentDateFormatted_returnsCorrectFormat` | Date du jour |
| `getDatePlusDaysFormatted_returnsCorrectFormat` | Date d'échéance (+30j) |
| `getCurrentTimestamp_returnsCorrectFormat` | Horodatage création |
| `isDateBeforeToday_pastDate` | Facture en retard |
| `isDateBeforeToday_futureDate` | Facture pas échue |
| `isDateBeforeToday_invalidDate` | Date invalide |
| `currentTimeMillis_returnsPositiveValue` | Timestamp valide |
| `formatDate_parseDate_roundTrip` | Cohérence affichage/lecture |

### 1.3 FormInputsValidatorTest (17 tests)
Tests de validation des formulaires.

| Test | Action utilisateur simulée |
|------|---------------------------|
| `validateName_validName_returnsNull` | Saisie nom valide |
| `validateName_emptyString_returnsError` | Nom vide → erreur |
| `validateName_null_returnsError` | Nom non renseigné → erreur |
| `validateName_singleCharacter_returnsNull` | Nom 1 caractère OK |
| `validateName_withNumbers_returnsNull` | Nom avec chiffres OK |
| `validateEmail_validEmail_returnsNull` | Email valide |
| `validateEmail_validEmailWithSubdomain_returnsNull` | Email sous-domaine |
| `validateEmail_validEmailWithPlus_returnsNull` | Email avec + |
| `validateEmail_validEmailWithDots_returnsNull` | Email avec points |
| `validateEmail_emptyString_returnsNull` | Email optionnel vide OK |
| `validateEmail_null_returnsNull` | Email non renseigné OK |
| `validateEmail_noAtSign_returnsError` | Email sans @ → erreur |
| `validateEmail_noDomain_returnsError` | Email sans domaine → erreur |
| `validateEmail_noLocalPart_returnsError` | Email sans nom → erreur |
| `validateEmail_spacesInEmail_returnsError` | Email avec espaces → erreur |
| `validateEmail_multipleDots_returnsNull` | Email plusieurs points |
| `validateName_withSpecialChars_returnsNull` | Nom avec caractères spéciaux |

### 1.4 BigDecimalExtensionsTest (18 tests)
Tests des conversions numériques.

| Test | Action utilisateur simulée |
|------|---------------------------|
| `stringToBigDecimal_integer` | Saisie prix entier "123" |
| `stringToBigDecimal_decimal` | Saisie prix "123.45" |
| `stringToBigDecimal_negative` | Saisie prix négatif |
| `doubleToBigDecimal` | Conversion interne |
| `longToBigDecimal` | Grands nombres |
| `intToBigDecimal` | Petits nombres |
| `setScale_roundDown` | Arrondi 123.456 → 123.46 |
| `setScale_noChange` | Pas d'arrondi nécessaire |
| `setScale_zeroDecimals` | Arrondi entier |
| `setScale_moreDecimals` | Ajout décimales |
| `stripTrailingZeros_*` | Affichage propre (123.4500 → 123.45) |
| `toIntKmp_*` | Conversion vers entier |

### 1.5 DocumentNumberTest (12 tests)
Tests d'incrémentation des numéros de documents.

| Test | Action utilisateur simulée |
|------|---------------------------|
| `incrementDocumentNumber_simpleNumber` | FA-001 → FA-002 |
| `incrementDocumentNumber_rolloverToFourDigits` | FA-999 → FA-1000 |
| `incrementDocumentNumber_withYear` | FA-2024-001 → FA-2024-002 |
| `incrementDocumentNumber_noPrefix` | 001 → 002 |
| `incrementDocumentNumber_singleDigit` | FA-1 → FA-002 |
| `incrementDocumentNumber_twoDigits` | FA-99 → FA-100 |
| `incrementDocumentNumber_noDigits` | FA-ABC inchangé |
| `incrementDocumentNumber_emptyString` | Vide inchangé |
| `incrementDocumentNumber_deliveryNote` | BL-2024-015 → BL-2024-016 |
| `incrementDocumentNumber_creditNote` | AV-2024-003 → AV-2024-004 |
| `incrementDocumentNumber_preservesPadding` | Padding 3 chiffres |
| `incrementDocumentNumber_largeNumber` | Grands numéros |

---

## 2. Tests Produits

### 2.1 ProductDataSourceTest (21 tests)
Tests de la gestion des produits.

| Test | Action utilisateur simulée |
|------|---------------------------|
| `createProduct_savesCorrectly` | Créer un nouveau produit |
| `createProduct_withAllFields` | Produit avec tous les champs |
| `createMultipleProducts_assignsUniqueIds` | Créer plusieurs produits |
| `fetchProduct_existingProduct` | Afficher un produit |
| `fetchProduct_nonExistent` | Produit introuvable |
| `fetchAllProducts_emptyList` | Liste vide |
| `fetchAllProducts_multipleProducts` | Liste avec produits |
| `updateProduct_changesName` | Modifier nom produit |
| `updateProduct_changesPrice` | Modifier prix produit |
| `updateProduct_nonExistent_noChange` | Modifier produit inexistant |
| `deleteProduct_removesFromList` | Supprimer un produit |
| `deleteProduct_nonExistent_noError` | Supprimer produit inexistant |
| `deleteProduct_leavesOthersIntact` | Supprimer sans affecter les autres |
| `duplicateProduct_createsNewWithSuffix` | Dupliquer un produit |
| `duplicateMultipleProducts` | Dupliquer plusieurs produits |
| `duplicateProduct_preservesPrice` | Dupliquer préserve le prix |
| `addAdditionalPrice_toProduct` | Ajouter prix client spécifique |
| `deleteAdditionalPrice_fromProduct` | Supprimer prix additionnel |
| `removeClientFromAdditionalPrice` | Retirer client du prix |
| `flowUpdates_onSave` | Réactivité à la création |
| `flowUpdates_onDelete` | Réactivité à la suppression |

---

## 3. Tests Clients/Émetteurs

### 3.1 ClientOrIssuerDataSourceTest (21 tests)
Tests de la gestion des clients et émetteurs.

| Test | Action utilisateur simulée |
|------|---------------------------|
| `createClient_savesCorrectly` | Créer un nouveau client |
| `createClient_withAddress` | Client avec adresse |
| `createClient_withMultipleAddresses` | Client plusieurs adresses |
| `createClient_withAllFields` | Client avec tous les champs |
| `createIssuer_savesCorrectly` | Créer un émetteur |
| `createIssuer_withCompanyIds` | Émetteur avec SIRET/TVA/RCS |
| `fetchClient_existingClient` | Afficher un client |
| `fetchClient_nonExistent` | Client introuvable |
| `fetchAllClients_filtersCorrectly` | Liste clients uniquement |
| `fetchAllIssuers_filtersCorrectly` | Liste émetteurs uniquement |
| `updateClient_changesName` | Modifier nom client |
| `updateClient_changesEmail` | Modifier email client |
| `updateClient_addsAddress` | Ajouter adresse |
| `updateClient_removesAddress` | Supprimer adresse |
| `deleteClient_removesFromList` | Supprimer un client |
| `deleteClient_leavesOthersIntact` | Supprimer sans affecter les autres |
| `duplicateClient_createsNewWithSuffix` | Dupliquer un client |
| `duplicateMultipleClients` | Dupliquer plusieurs clients |
| `duplicateClient_preservesAddresses` | Dupliquer préserve les adresses |
| `getLastCreatedClientId_returnsCorrectId` | Récupérer dernier ID |
| `getLastCreatedClientId_nullWhenEmpty` | Liste vide → null |

---

## 4. Tests Factures

### 4.1 InvoiceDataSourceTest (51 tests)
Tests de la gestion des factures.

#### Création
| Test | Action utilisateur simulée |
|------|---------------------------|
| `createInvoice_createsEmptyDocument` | Créer nouvelle facture |
| `createMultipleInvoices_assignsUniqueIds` | Créer plusieurs factures |
| `createInvoice_generatesSequentialNumbers` | Numéros FA-001, FA-002... |
| `createInvoice_setsDateToToday` | Date par défaut = aujourd'hui |
| `createInvoice_setsDueDateTo30DaysFromNow` | Échéance = J+30 |
| `createInvoice_setsCreatedDate` | Horodatage création |
| `createInvoice_setsDefaultFooter` | Footer par défaut |
| `createInvoice_customFooterCanBeSet` | Footer personnalisable |

#### Produits
| Test | Action utilisateur simulée |
|------|---------------------------|
| `addProductToInvoice_savesCorrectly` | Ajouter produit à facture |
| `addMultipleProductsToInvoice` | Ajouter plusieurs produits |
| `removeProductFromInvoice` | Retirer produit de facture |
| `removeProductFromInvoice_leavesOthers` | Retirer sans affecter les autres |
| `addProductWithDeliveryNoteInfo_savesLinkedInfo` | Produit avec info BL |

#### Client/Émetteur
| Test | Action utilisateur simulée |
|------|---------------------------|
| `addClientToInvoice` | Associer client à facture |
| `addIssuerToInvoice` | Associer émetteur à facture |
| `addBothClientAndIssuerToInvoice` | Client et émetteur |
| `removeClientFromInvoice` | Retirer client |

#### Modification
| Test | Action utilisateur simulée |
|------|---------------------------|
| `updateInvoice_changesNumber` | Modifier numéro facture |
| `updateInvoice_changesDate` | Modifier date facture |
| `updateInvoice_changesDueDate` | Modifier échéance |

#### Suppression & Duplication
| Test | Action utilisateur simulée |
|------|---------------------------|
| `deleteInvoice_removesFromList` | Supprimer facture |
| `deleteMultipleInvoices` | Supprimer plusieurs factures |
| `duplicateInvoice_copiesAllData` | Dupliquer facture |
| `duplicateInvoice_incrementsNumber` | Nouveau numéro à la copie |
| `duplicateInvoice_setsTagToDraft` | Copie en brouillon |

#### Tags
| Test | Action utilisateur simulée |
|------|---------------------------|
| `setTag_paid` | Marquer comme payée |
| `setTag_sent` | Marquer comme envoyée |
| `setTag_reminder` | Marquer relance |
| `setTag_cancelled` | Marquer comme annulée |
| `removeTag` | Retirer le tag |
| `markAsPaid_setsCorrectTag` | Bouton "Payée" |

#### Réorganisation
| Test | Action utilisateur simulée |
|------|---------------------------|
| `reorderProducts_movesCorrectly` | Drag & drop produits |
| `reorderProducts_updatesOrder` | Ordre sauvegardé |

#### Conversion BL → Facture
| Test | Action utilisateur simulée |
|------|---------------------------|
| `convertDeliveryNotesToInvoice_createsNewInvoice` | Bouton "Facturer" |
| `convertDeliveryNotesToInvoice_setsDateToToday` | Date facture = aujourd'hui |
| `convertDeliveryNotesToInvoice_copiesClientAndIssuer` | Copie client/émetteur du BL |
| `convertDeliveryNotesToInvoice_productsLinkedToDeliveryNote` | Produits liés au BL |
| `convertMultipleDeliveryNotesToInvoice_combinesProducts` | Facturer plusieurs BL |
| `convertMultipleDeliveryNotesToInvoice_eachProductLinkedToItsDeliveryNote` | Chaque produit lié à son BL |
| `convertDeliveryNotesToInvoice_setsDefaultFooter` | Footer dans facture convertie |

#### Facture Rectificative (Annule et Remplace)
| Test | Action utilisateur simulée |
|------|---------------------------|
| `createCorrectiveInvoice_createsNewInvoice` | Créer facture rectificative |
| `createCorrectiveInvoice_hasCancelAndReplaceText` | Mention "Annule et remplace FA-XXX" |
| `createCorrectiveInvoice_originalMarkedAsCancelled` | Facture originale annulée |
| `createCorrectiveInvoice_correctiveIsDraft` | Rectificative en brouillon |
| `createCorrectiveInvoice_copiesClientAndIssuer` | Copie client/émetteur |
| `createCorrectiveInvoice_copiesProducts` | Copie produits |
| `createCorrectiveInvoice_incrementsNumber` | Nouveau numéro FA-002 |
| `createCorrectiveInvoice_customCancelAndReplaceText` | Texte personnalisable |
| `createCorrectiveInvoice_paidInvoiceCanBeCorrected` | Rectifier facture payée |

#### Flow
| Test | Action utilisateur simulée |
|------|---------------------------|
| `flowUpdates_onCreateInvoice` | Réactivité création |
| `flowUpdates_onDeleteInvoice` | Réactivité suppression |

---

## 5. Tests Bons de Livraison

### 5.1 DeliveryNoteDataSourceTest (22 tests)
Tests de la gestion des bons de livraison.

#### Création
| Test | Action utilisateur simulée |
|------|---------------------------|
| `createDeliveryNote_createsEmptyDocument` | Créer nouveau BL |
| `createDeliveryNote_setsDateToToday` | Date par défaut = aujourd'hui |
| `createDeliveryNote_setsCreatedDate` | Horodatage création |
| `createMultipleDeliveryNotes_assignsUniqueIds` | Créer plusieurs BL |
| `createDeliveryNote_generatesSequentialNumbers` | Numéros BL-001, BL-002... |

#### Produits
| Test | Action utilisateur simulée |
|------|---------------------------|
| `addProductToDeliveryNote_savesCorrectly` | Ajouter produit au BL |
| `addMultipleProductsToDeliveryNote` | Ajouter plusieurs produits |
| `removeProductFromDeliveryNote` | Retirer produit du BL |

#### Client/Émetteur
| Test | Action utilisateur simulée |
|------|---------------------------|
| `addClientToDeliveryNote` | Associer client au BL |
| `addIssuerToDeliveryNote` | Associer émetteur au BL |
| `removeClientFromDeliveryNote` | Retirer client |

#### Modification
| Test | Action utilisateur simulée |
|------|---------------------------|
| `updateDeliveryNote_changesNumber` | Modifier numéro BL |
| `updateDeliveryNote_changesDate` | Modifier date BL |

#### Suppression & Duplication
| Test | Action utilisateur simulée |
|------|---------------------------|
| `deleteDeliveryNote_removesFromList` | Supprimer BL |
| `deleteMultipleDeliveryNotes` | Supprimer plusieurs BL |
| `duplicateDeliveryNote_copiesAllData` | Dupliquer BL |
| `duplicateDeliveryNote_incrementsNumber` | Nouveau numéro à la copie |
| `duplicateDeliveryNote_setsTagToDraft` | Copie en brouillon |

#### Réorganisation
| Test | Action utilisateur simulée |
|------|---------------------------|
| `reorderProducts_movesCorrectly` | Drag & drop produits |
| `reorderProducts_updatesOrder` | Ordre sauvegardé |

#### Flow
| Test | Action utilisateur simulée |
|------|---------------------------|
| `flowUpdates_onCreateDeliveryNote` | Réactivité création |
| `flowUpdates_onDeleteDeliveryNote` | Réactivité suppression |

---

## 6. Tests Avoirs

### 6.1 CreditNoteDataSourceTest (27 tests)
Tests de la gestion des avoirs.

#### Création
| Test | Action utilisateur simulée |
|------|---------------------------|
| `createCreditNote_createsEmptyDocument` | Créer nouvel avoir |
| `createCreditNote_setsDateToToday` | Date par défaut = aujourd'hui |
| `createCreditNote_setsCreatedDate` | Horodatage création |
| `createMultipleCreditNotes_assignsUniqueIds` | Créer plusieurs avoirs |
| `createCreditNote_generatesSequentialNumbers` | Numéros AV-001, AV-002... |

#### Produits
| Test | Action utilisateur simulée |
|------|---------------------------|
| `addProductToCreditNote_savesCorrectly` | Ajouter produit à l'avoir |
| `addProductToCreditNote_withLinkedDeliveryNoteInfo` | Produit avec info BL |
| `addMultipleProductsToCreditNote` | Ajouter plusieurs produits |
| `removeProductFromCreditNote` | Retirer produit de l'avoir |

#### Client/Émetteur
| Test | Action utilisateur simulée |
|------|---------------------------|
| `addClientToCreditNote` | Associer client à l'avoir |
| `addIssuerToCreditNote` | Associer émetteur à l'avoir |
| `removeClientFromCreditNote` | Retirer client |

#### Conversion Facture → Avoir
| Test | Action utilisateur simulée |
|------|---------------------------|
| `convertInvoiceToCreditNote_createsNewCreditNote` | Créer avoir depuis facture |
| `convertInvoiceToCreditNote_setsDateToToday` | Date avoir = aujourd'hui |
| `convertInvoiceToCreditNote_copiesClientAndIssuer` | Copie client/émetteur |
| `convertInvoiceToCreditNote_copiesProductsWithLinkedInfo` | Produits liés à la facture |
| `convertInvoiceToCreditNote_linksToOriginalInvoice` | Référence facture originale |

#### Modification
| Test | Action utilisateur simulée |
|------|---------------------------|
| `updateCreditNote_changesNumber` | Modifier numéro avoir |
| `updateCreditNote_changesDate` | Modifier date avoir |

#### Suppression & Duplication
| Test | Action utilisateur simulée |
|------|---------------------------|
| `deleteCreditNote_removesFromList` | Supprimer avoir |
| `deleteMultipleCreditNotes` | Supprimer plusieurs avoirs |
| `duplicateCreditNote_copiesAllData` | Dupliquer avoir |
| `duplicateCreditNote_incrementsNumber` | Nouveau numéro à la copie |
| `duplicateCreditNote_setsTagToDraft` | Copie en brouillon |

#### Réorganisation
| Test | Action utilisateur simulée |
|------|---------------------------|
| `reorderProducts_movesCorrectly` | Drag & drop produits |

#### Flow
| Test | Action utilisateur simulée |
|------|---------------------------|
| `flowUpdates_onCreateCreditNote` | Réactivité création |
| `flowUpdates_onDeleteCreditNote` | Réactivité suppression |

---

## 7. Résumé

| Catégorie | Classe de test | Tests |
|-----------|---------------|-------|
| Utilitaires | PriceCalculationsTest | 8 |
| Utilitaires | DateUtilsTest | 17 |
| Utilitaires | FormInputsValidatorTest | 17 |
| Utilitaires | BigDecimalExtensionsTest | 18 |
| Utilitaires | DocumentNumberTest | 12 |
| Produits | ProductDataSourceTest | 21 |
| Clients/Émetteurs | ClientOrIssuerDataSourceTest | 21 |
| Factures | InvoiceDataSourceTest | 51 |
| Bons de Livraison | DeliveryNoteDataSourceTest | 22 |
| Avoirs | CreditNoteDataSourceTest | 27 |
| **TOTAL** | **10 classes** | **214** |

---

## 8. Couverture des Actions Utilisateur

### Actions couvertes

#### Produits
- [x] Créer/modifier/supprimer un produit
- [x] Ajouter prix additionnel à un produit
- [x] Retirer un client d'un prix additionnel
- [x] Dupliquer produits

#### Clients/Émetteurs
- [x] Créer/modifier/supprimer un client
- [x] Créer/modifier/supprimer un émetteur
- [x] Gérer les adresses client (ajouter/supprimer)
- [x] Dupliquer clients

#### Factures
- [x] Créer une facture (date = aujourd'hui)
- [x] Ajouter/supprimer produits dans facture
- [x] Associer client/émetteur à facture
- [x] Retirer client/émetteur de facture
- [x] Modifier numéro/date/échéance
- [x] Footer par défaut affiché
- [x] Dupliquer factures
- [x] Supprimer factures
- [x] Ajouter/retirer tags (payée, envoyée, relance, annulée)
- [x] Marquer comme payée
- [x] Réorganiser produits (drag & drop)
- [x] Convertir BL en facture ("Facturer")
- [x] Conversion multiple BL → 1 facture
- [x] Produits liés à leur BL d'origine
- [x] Créer facture rectificative (annule et remplace)
- [x] Facture originale marquée comme annulée
- [x] Texte "Annule et remplace" dans champ libre

#### Bons de Livraison
- [x] Créer un BL (date = aujourd'hui)
- [x] Ajouter/supprimer produits dans BL
- [x] Associer client/émetteur au BL
- [x] Modifier numéro/date
- [x] Dupliquer BL
- [x] Supprimer BL
- [x] Réorganiser produits

#### Avoirs
- [x] Créer un avoir (date = aujourd'hui)
- [x] Ajouter/supprimer produits dans avoir
- [x] Associer client/émetteur à l'avoir
- [x] Créer avoir depuis facture
- [x] Produits liés à la facture originale
- [x] Référence à la facture originale
- [x] Dupliquer avoirs
- [x] Supprimer avoirs

#### Validation & Calculs
- [x] Validation formulaires (nom, email)
- [x] Calculs de prix HT/TTC
- [x] Incrémentation numéros de documents
- [x] Génération numéros séquentiels

### Actions non testées (UI uniquement)

- [ ] Export PDF
- [ ] Partage par email
- [ ] Navigation entre écrans
- [ ] Animations
- [ ] Affichage des listes avec filtres

---

## 9. Fake Data Sources

Les tests utilisent des implémentations "fake" des data sources pour simuler la persistance des données sans accéder à la base SQLite :

| Fake | Interface implémentée |
|------|----------------------|
| `FakeProductDataSource` | `ProductLocalDataSourceInterface` |
| `FakeClientOrIssuerDataSource` | `ClientOrIssuerLocalDataSourceInterface` |
| `FakeInvoiceDataSource` | `InvoiceLocalDataSourceInterface` |
| `FakeDeliveryNoteDataSource` | `DeliveryNoteLocalDataSourceInterface` |
| `FakeCreditNoteDataSource` | `CreditNoteLocalDataSourceInterface` |

Ces fakes sont situés dans `shared/src/commonTest/kotlin/com/a4a/g8invoicing/fakes/`.
