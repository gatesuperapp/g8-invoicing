# Audit modales — chantier design system post-1.9

Diagnostic à l'appui d'une future harmonisation. **Pas dans le scope
release/1.9.** À reprendre plus tard comme lot dédié.

## 1. Le style canonique de référence

**Modale "Format d'export"** (choix PDF / Factur-X / CII à l'export d'une
facture) — c'est le style visé pour toutes les modales de l'app.

**Fichier :** `shared/src/commonMain/kotlin/com/a4a/g8invoicing/ui/screens/shared/DocumentAddEdit.kt:1169-1262`
(`ExportFormatChooserDialog`)

### Tokens design extraits

| Élément | Style |
|---|---|
| Titre | `MaterialTheme.typography.textScreenTitle.copy(fontSize = 18.sp)` — 18sp, poids implicite (medium), couleur `AppColors.textPrimary` (#1A1A1A) |
| Corps | `MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary)` — 14sp, `AppColors.textSecondary` (#57544F), lineHeight 20sp |
| CTA principal | `Button()` — `containerColor = AppColors.buttonActive` (#932092, violet), `contentColor = AppColors.textOnAccent` (blanc) |
| CTA secondaire / annuler | `OutlinedButton()` — `contentColor = AppColors.textLink` (#932092, violet), pas de fond |
| Padding externe | 24dp horizontal + vertical |
| Titre → corps | Spacer 12dp |
| Corps → premier bouton | Spacer 24dp |
| Entre boutons | Spacer 8dp |

Note technique : cette modale utilise `Dialog(DialogProperties(usePlatformDefaultWidth = false))` + Box custom (pas `AlertDialog`), permettant le contrôle précis des paddings et coins.

## 2. Inventaire (~30 modales dans `shared/`)

### Info / warning
- `NavGraphInvoiceAddEdit.kt:131` — version mismatch émetteur
- `NavGraphInvoiceAddEdit.kt:217` — version mismatch client
- `NavGraphInvoiceAddEdit.kt:588` — modification comptes bancaires (celle qu'on vient d'ajouter)
- `Account.kt:509` — succès export/backup
- `InfoTooltipButton.kt:111` — dismiss d'un tooltip d'aide

### Confirmation (Oui/Non)
- `Account.kt:412` — suppression compte
- `Account.kt:463` — suppression compte en cours (loader)
- `Account.kt:537` — envoi DB par email

### Actions destructives
- `AlertDialogDeleteDocument.kt:37` — supprimer document
- `NavGraphCreditNoteAddEdit.kt:109`, `NavGraphQuoteAddEdit.kt:115` — via l'helper ci-dessus

### Choix (pickers)
- `ExportFormatChooserDialog` (1169-1262) — **référence canonique**

### Validation form
- `FormValidationErrorDialog.kt:46` — erreurs pré-save

### Post-action
- `AlertDialogInvoiceCreated.kt:32` — facture créée, voir ?

### Onboarding / migration
- `OnboardingDialog.kt:265, 291, 717`
- `OnboardingMigration19Dialog.kt:734, 768, 989`

### Erreurs
- `Account.kt:1055` — erreur export
- `LogoPickerComponent.kt:123` — erreur upload logo

## 3. Helpers partagés existants

| Fichier | Signature | Portée |
|---|---|---|
| `AlertDialogErrorOrInfo.kt:15` | `(onDismiss, onConfirm, message, confirmText)` | Bouton unique, pas de titre, textContentColor forcé Black |
| `AlertDialogDeleteDocument.kt:30` | `(onDismiss, onConfirm, isInvoice)` | Deux boutons, ClickableText special pour invoice |
| `AlertDialogInvoiceCreated.kt:26` | `(onDismiss, onConfirm, titleText, buttonText)` | Un bouton centré |
| `FormValidationErrorDialog.kt:38` | `(rawMessages, onDismiss)` | Titre + liste scrollable, un bouton |

Aucun de ces helpers ne matche le style canonique de `ExportFormatChooserDialog`.

## 4. Divergences observées

### Typographie titre
- `ExportFormatChooserDialog` : `textScreenTitle.copy(fontSize = 18.sp)` explicite
- `FormValidationErrorDialog` / `InfoTooltipButton` : `Text()` brut → défaut Material3 (plus gros)
- `AlertDialogErrorOrInfo` : pas de titre du tout
- `AlertDialogDeleteDocument` : `Text()` brut sans style

### Boutons
- `ExportFormatChooserDialog` : `Button()` violet plein + `OutlinedButton()` contour violet
- `AlertDialogDeleteDocument` : `Button()` brut sans couleurs (défaut Material3, plutôt sombre)
- `Account.kt` suppression : `TextButton()` avec `color = ColorRedLate` pour confirm, violet pour dismiss
- `Account.kt` backup : les deux boutons `TextButton()` violet
- `FormValidationErrorDialog`, `InfoTooltipButton` : `Button()` brut
- `LogoPickerComponent` : `TextButton()` texte noir

### Couleur du bouton "annuler / dismiss"
- Violet `textLink` (Account.kt)
- Noir (LogoPickerComponent)
- Implicite/défaut (helpers AlertDialogXxx)
- Rouge (Account delete confirm — c'est intentionnel pour destructif)

### Container `AlertDialog` vs `Dialog` custom
- `ExportFormatChooserDialog` : `Dialog(usePlatformDefaultWidth = false)` + Box custom → contrôle fin
- CII export flow : `Dialog()` default width
- Toutes les autres : `AlertDialog()` Material3 standard

### Paddings
- `ExportFormatChooserDialog` : 32dp outer, 24dp inner, spacers précis
- Helpers `AlertDialog*` : paddings implicites Material3
- `FormValidationErrorDialog` : `verticalScroll()` sans padding explicite

## 5. Recommandation d'unification post-1.9

### Créer 3 helpers standardisés couvrant les cas d'usage principaux

```kotlin
// Un bouton confirm (info / succès / erreur non-actionnable)
@Composable
fun AppInfoDialog(
    title: String? = null,
    body: String,
    confirmText: String = "OK",
    onDismiss: () -> Unit,
)

// Deux boutons (confirm + cancel)
@Composable
fun AppConfirmDialog(
    title: String,
    body: String,
    confirmText: String,
    cancelText: String = "Annuler",
    destructive: Boolean = false,  // → confirm en rouge
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
)

// N choix (comme le picker export)
@Composable
fun AppChoiceDialog(
    title: String,
    body: String? = null,
    choices: List<Choice>,  // label + primary/secondary + onClick
    onDismiss: () -> Unit,
)
```

Tous trois basés sur le pattern `Dialog(usePlatformDefaultWidth = false)` + Box custom pour matcher pixel-per-pixel `ExportFormatChooserDialog`.

### Migration à faire

1. Extraire les tokens dans un objet `ModalTokens` (typography, colors, spacings) — évite la duplication
2. Créer les 3 helpers, tester sur 1-2 sites
3. Migrer progressivement les ~30 sites listés section 2
4. Retirer les helpers legacy (`AlertDialogErrorOrInfo`, `AlertDialogInvoiceCreated`, `AlertDialogDeleteDocument`) une fois tous leurs call-sites migrés

Effort estimé : 4-6h focus. Sans risque fonctionnel (UI-only), mais tests visuels manuels sur chaque site.
