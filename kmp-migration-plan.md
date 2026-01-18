# Plan de Migration KMP - G8 Invoicing

## Vue d'ensemble

Migration de l'app Android vers Kotlin Multiplatform pour supporter iOS.

**Objectifs :**
- Partager la logique métier entre Android et iOS
- Remplacer Hilt par Koin (KMP-compatible) - **FAIT**
- Garder iText sur Android, utiliser PDFKit sur iOS via expect/actual
- Utiliser Compose Multiplatform pour l'UI

---

## ÉTAT D'AVANCEMENT GLOBAL

| Phase | Sujet | Status |
|-------|-------|--------|
| Préparatoire | 0A - Hilt → Koin | ✅ Terminé |
| Préparatoire | 0B - Lottie → Compottie | ✅ Terminé |
| KMP | 1 - Setup Projet KMP | ✅ Terminé |
| KMP | 2 - DI Koin KMP | ✅ Terminé |
| KMP | 3 - Extraction Code Partagé | ✅ Terminé |
| KMP | 4 - expect/actual Database | ✅ Terminé |
| KMP | 5 - expect/actual Storage | ✅ Terminé |
| KMP | 6 - expect/actual PDF | 🟡 Partiel |
| KMP | 7 - Navigation KMP | 🟡 Partiel (TopBar, Screens List migrés) |
| KMP | 8 - UI Compose Multiplatform | 🟡 Partiel (ViewModels + Screens List migrés) |
| KMP | 9 - Tests et Finalisation | ❌ Non commencé |

---

## Nouvelle Arborescence

```
g8-invoicing/
├── build.gradle.kts                    # Root build config
├── settings.gradle.kts                 # Module declarations
│
├── shared/                             # MODULE KMP PARTAGÉ
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/com/a4a/g8invoicing/
│       │   ├── data/
│       │   │   ├── models/             # États et DTOs
│       │   │   │   ├── InvoiceState.kt
│       │   │   │   ├── ProductState.kt
│       │   │   │   ├── ClientOrIssuerState.kt
│       │   │   │   ├── DocumentState.kt
│       │   │   │   └── ...
│       │   │   ├── datasource/         # Interfaces + Implémentations DataSource
│       │   │   │   ├── InvoiceLocalDataSourceInterface.kt
│       │   │   │   ├── InvoiceLocalDataSource.kt
│       │   │   │   └── ...
│       │   │   └── repository/         # Logique métier partagée
│       │   │       └── DocumentRepository.kt
│       │   │
│       │   ├── domain/
│       │   │   └── utils/              # Fonctions de calcul pures
│       │   │       ├── PriceCalculations.kt
│       │   │       ├── DocumentNumberUtils.kt
│       │   │       └── DateFormatUtils.kt
│       │   │
│       │   ├── di/                     # Modules Koin partagés
│       │   │   └── SharedModule.kt
│       │   │
│       │   ├── pdf/                    # Interface PDF expect/actual
│       │   │   └── PdfGenerator.kt     # expect class PdfGenerator
│       │   │
│       │   └── ui/                     # UI COMPOSE MULTIPLATFORM (PARTAGÉ)
│       │       ├── navigation/         # Navigation Compose Multiplatform
│       │       │   ├── AppNavigator.kt
│       │       │   └── Screen.kt
│       │       ├── screens/            # Tous les écrans Compose
│       │       │   ├── InvoiceListScreen.kt
│       │       │   ├── InvoiceAddEditScreen.kt
│       │       │   ├── DeliveryNoteListScreen.kt
│       │       │   ├── ProductListScreen.kt
│       │       │   ├── ClientOrIssuerListScreen.kt
│       │       │   └── ...
│       │       ├── viewmodels/         # ViewModels Koin (restent des ViewModels)
│       │       │   ├── InvoiceListViewModel.kt
│       │       │   ├── InvoiceAddEditViewModel.kt
│       │       │   └── ...
│       │       ├── shared/             # Composants UI réutilisables
│       │       │   ├── FormInput.kt
│       │       │   ├── DocumentBottomSheet.kt
│       │       │   └── ...
│       │       └── theme/              # Thème Material3
│       │           ├── Theme.kt
│       │           ├── Color.kt
│       │           └── Typography.kt
│       │
│       ├── commonMain/composeResources/  # Resources Compose Multiplatform
│       │   ├── font/
│       │   │   ├── helvetica.ttf
│       │   │   └── helveticabold.ttf
│       │   ├── drawable/
│       │   │   └── img_paid.png
│       │   └── values/
│       │       ├── strings.xml           # Strings multiplatform
│       │       └── strings-fr.xml        # Traductions FR
│       │
│       ├── androidMain/kotlin/com/a4a/g8invoicing/
│       │   ├── data/driver/
│       │   │   └── DatabaseDriverFactory.kt  # actual AndroidSqliteDriver
│       │   ├── di/
│       │   │   └── AndroidModule.kt    # Koin module Android-specific
│       │   ├── pdf/
│       │   │   └── PdfGenerator.android.kt  # actual avec iText
│       │   └── storage/
│       │       └── FileStorage.android.kt   # actual MediaStore
│       │
│       └── iosMain/kotlin/com/a4a/g8invoicing/
│           ├── data/driver/
│           │   └── DatabaseDriverFactory.kt  # actual NativeSqliteDriver
│           ├── di/
│           │   └── IosModule.kt        # Koin module iOS-specific
│           ├── pdf/
│           │   └── PdfGenerator.ios.kt  # actual avec PDFKit
│           └── storage/
│               └── FileStorage.ios.kt   # actual Documents directory
│
├── androidApp/                         # MODULE ANDROID (minimal)
│   ├── build.gradle.kts
│   └── src/main/
│       ├── kotlin/com/a4a/g8invoicing/
│       │   ├── G8Invoicing.kt          # Application (init Koin)
│       │   └── MainActivity.kt         # Entry point → appelle shared UI
│       ├── res/                        # Resources Android-specific
│       │   └── values/strings.xml
│       └── AndroidManifest.xml
│
└── iosApp/                             # MODULE iOS (minimal)
    ├── iosApp.xcodeproj
    └── iosApp/
        ├── iOSApp.swift                # Entry point
        ├── ContentView.swift           # Host Compose Multiplatform → appelle shared UI
        └── Info.plist
```

---

## PHASE PRÉPARATOIRE (sur projet Android actuel) - ✅ TERMINÉE

---

### SUJET 0A : Migration Hilt → Koin (Android) - ✅ TERMINÉ

#### 0A.1 - Ajout de Koin ✅
- [x] Ajouter dépendance `io.insert-koin:koin-android:3.5.0` dans `app/build.gradle.kts`
- [x] Ajouter dépendance `io.insert-koin:koin-androidx-compose:3.5.0`
- [x] Créer `di/KoinModules.kt` avec les mêmes providers que `AppModule.kt`

#### 0A.2 - Migration progressive des ViewModels ✅
- [x] Migrer tous les ViewModels : retirer @HiltViewModel, utiliser constructeur simple
- [x] Utiliser `koinViewModel()` dans les screens au lieu de `hiltViewModel()`

#### 0A.3 - Migration Application et Activity ✅
- [x] Retirer `@HiltAndroidApp` de `G8Invoicing.kt`
- [x] Ajouter `startKoin { androidContext(this@G8Invoicing); modules(appModule) }`
- [x] Retirer `@AndroidEntryPoint` de `MainActivity.kt`

#### 0A.4 - Nettoyage Hilt ✅
- [x] Supprimer `di/AppModule.kt` (remplacé par KoinModules)
- [x] Retirer dépendances Hilt du `build.gradle.kts`
- [x] Retirer plugin `kotlin-kapt`
- [x] Retirer plugin `com.google.dagger.hilt.android`
- [x] Ajouter `navigation-compose` (était transitif via Hilt)
- [x] Vérifier que l'app compile et fonctionne
- [x] Commit stable "Migration Hilt → Koin"

---

### SUJET 0B : Migration Lottie → Compottie - ✅ TERMINÉ

> **Note:** Utilisé Compottie (lib KMP) au lieu d'animations Compose natives

#### 0B.1 - Analyser les animations actuelles ✅
- [x] Identifier tous les fichiers .json Lottie utilisés (bat_wavy_arms, bat_smiling_eyes, bat_openmouth, bat_kiss_gif)
- [x] Documenter ce que chaque animation fait
- [x] Identifier les paramètres utilisés (iterations, modifier)

#### 0B.2 - Intégrer Compottie ✅
- [x] Ajouter dépendance `io.github.alexzhirkevich:compottie:2.0.2` dans shared
- [x] Ajouter dépendance `io.github.alexzhirkevich:compottie-resources:2.0.2`
- [x] Exclure kotlinx-datetime de Compottie (conflit de version)
- [x] Copier les fichiers JSON vers `shared/commonMain/composeResources/files/`
- [x] Créer `shared/commonMain/ui/shared/BatAnimation.kt` avec enum `BatAnimationType`

#### 0B.3 - Remplacer BatAnimation ✅
- [x] Modifier `InvoiceList.kt` : utiliser nouveau BatAnimation
- [x] Modifier `DeliveryNoteList.kt`
- [x] Modifier `Account.kt`
- [x] Modifier `About.kt`
- [x] Modifier `DocumentBottomSheetProductListChosen.kt`

#### 0B.4 - Nettoyage Lottie ✅
- [x] Supprimer ancien `ui/shared/BatAnimation.kt` de app
- [x] Garder les fichiers .json dans `res/raw/` (pour compatibilité)
- [x] Retirer dépendance `com.airbnb.android:lottie-compose` du `build.gradle.kts`
- [x] Vérifier que l'app compile et fonctionne

---

## PHASE KMP (après migrations préparatoires)

---

### SUJET 1 : Setup Projet KMP - ✅ TERMINÉ

#### 1.1 - Structure des modules ✅
- [x] Créer le dossier `shared/` à la racine
- [x] Créer `shared/build.gradle.kts` avec configuration KMP
- [x] Garder `app/` au lieu de `androidApp/` (fonctionne)
- [x] Mettre à jour `settings.gradle.kts` pour déclarer les modules `shared` et `app`
- [x] Mettre à jour `app/build.gradle.kts` pour dépendre de `:shared`

#### 1.2 - Configuration Gradle KMP ✅
- [x] Ajouter le plugin `kotlin-multiplatform` dans `shared/build.gradle.kts`
- [x] Configurer les targets : `androidTarget()`, `iosX64()`, `iosArm64()`, `iosSimulatorArm64()`
- [x] Configurer les source sets : `commonMain`, `androidMain`, `iosMain`
- [x] Ajouter les dépendances communes (coroutines, datetime, bignum)
- [x] Vérifier que le projet compile

#### 1.3 - Setup iOS ✅
- [x] Créer le dossier `iosApp/`
- [x] Créer `iOSApp.swift` (entry point avec init Koin)
- [x] Créer `ContentView.swift` (host Compose Multiplatform via UIViewControllerRepresentable)
- [x] Créer `shared/commonMain/App.kt` (composable racine)
- [x] Créer `shared/iosMain/MainViewController.kt` (ComposeUIViewController)
- [x] Créer `shared/iosMain/di/KoinHelper.kt` (initKoin pour Swift)
- [ ] Générer le projet Xcode (.xcodeproj) - à faire manuellement dans Xcode

---

### SUJET 2 : Restructuration DI pour KMP - ✅ TERMINÉ

#### 2.1 - Créer modules Koin KMP ✅
- [x] Créer `shared/commonMain/di/SharedModule.kt` (dépendances partagées)
- [x] Créer `shared/androidMain/di/AndroidModule.kt` (SqlDriver, Context-dependent)
- [x] Créer `shared/iosMain/di/IosModule.kt` (SqlDriver iOS)
- [x] Adapter le module Koin existant pour la structure KMP

#### 2.2 - Migration Koin pour KMP ✅
- [x] Koin fonctionne avec la structure actuelle
- [x] Les ViewModels restent des ViewModels

---

### SUJET 3 : Extraction du Code Partagé - ✅ TERMINÉ

#### 3.1 - Déplacer les Models/States ✅
- [x] Créer `shared/commonMain/ui/states/`
- [x] Déplacer `ClientOrIssuerState.kt`
- [x] Déplacer `DocumentProductState.kt`
- [x] Déplacer `DocumentState.kt`
- [x] Déplacer `InvoiceState.kt`
- [x] Déplacer `DeliveryNoteState.kt`
- [x] Déplacer `CreditNoteState.kt`
- [x] Déplacer `ProductState.kt`
- [x] Déplacer `CompanyDataState.kt`
- [x] Déplacer `Message.kt`
- [x] Déplacer les UiState (`InvoicesUiState`, `DeliveryNotesUiState`, etc.)
- [x] Créer `shared/commonMain/data/models/`
- [x] Déplacer les enums (`ClientOrIssuerType`, `DocumentTag`, `DocumentType`, `ScreenElement`)
- [x] Mettre à jour les imports dans `app`

#### 3.2 - Déplacer les Interfaces DataSource ✅
- [x] Créer `shared/commonMain/data/` (interfaces dans data/)
- [x] Déplacer `InvoiceLocalDataSourceInterface.kt`
- [x] Déplacer `DeliveryNoteLocalDataSourceInterface.kt`
- [x] Déplacer `CreditNoteLocalDataSourceInterface.kt`
- [x] Déplacer `ProductLocalDataSourceInterface.kt`
- [x] Déplacer `ProductTaxLocalDataSourceInterface.kt`
- [x] Déplacer `ClientOrIssuerLocalDataSourceInterface.kt`
- [x] Déplacer `AlertDialogDataSourceInterface.kt`
- [x] Créer `PersonType.kt` et `TagUpdateOrCreationCase.kt` dans `data/models/`
- [x] Mettre à jour les imports

#### 3.3 - Extraire les Fonctions Utilitaires Pures ✅
- [x] Créer `shared/commonMain/utils/`
- [x] Créer `DateUtils.kt` (getCurrentDate, formatters)
- [x] Créer `BigDecimalExtensions.kt` (toPlainString, setScale, toFormattedPrice)
- [x] Créer `DocumentUtils.kt` avec:
  - `incrementDocumentNumber()` - incrémentation numéro de document
  - `calculateDocumentPrices()` - calcul des totaux HT/TTC/taxes

#### 3.4 - Vérification ✅
- [x] Vérifier que l'app Android compile
- [x] Vérifier que le module shared compile (iOS + Android)

---

### SUJET 4 : expect/actual - Database - ✅ TERMINÉ

#### 4.1 - DatabaseDriverFactory ✅
- [x] Créer `shared/commonMain/data/driver/DatabaseDriverFactory.kt` avec `expect`
- [x] Créer `shared/androidMain/data/driver/DatabaseDriverFactory.kt` avec `actual` (AndroidSqliteDriver)
- [x] Créer `shared/iosMain/data/driver/DatabaseDriverFactory.kt` avec `actual` (NativeSqliteDriver)
- [x] SQLDelight configuré dans shared

#### 4.2 - Migration des DataSources ✅
- [x] Déplacer `InvoiceLocalDataSource.kt` vers `shared/commonMain`
- [x] Déplacer `DeliveryNoteLocalDataSource.kt`
- [x] Déplacer `CreditNoteLocalDataSource.kt`
- [x] Déplacer `ProductLocalDataSource.kt`
- [x] Déplacer `ProductTaxLocalDataSource.kt`
- [x] Déplacer `ClientOrIssuerLocalDataSource.kt`
- [x] Déplacer `AlertDialogLocalDataSource.kt`
- [x] Créer `DataSourceHelpers.kt` pour fonctions utilitaires partagées
- [x] Créer `Logger` expect/actual pour remplacer android.util.Log
- [x] Créer `IoDispatcher` expect/actual pour remplacer Dispatchers.IO
- [x] Vérifier compilation Android et iOS

---

### SUJET 5 : expect/actual - Storage - ✅ TERMINÉ

#### 5.1 - DataStore Migration ✅
- [x] Analyser l'usage actuel de DataStore (HAS_SEEN_POPUP, LAST_SEEN_VERSION)
- [x] Ajouter `multiplatform-settings` library dans shared/build.gradle.kts
- [x] Créer `shared/commonMain/data/settings/AppSettings.kt`
- [x] Créer `shared/commonMain/data/settings/SettingsFactory.kt` (expect)
- [x] Créer `shared/androidMain/data/settings/SettingsFactory.kt` (actual - SharedPreferences)
- [x] Créer `shared/iosMain/data/settings/SettingsFactory.kt` (actual - NSUserDefaults)
- [x] Enregistrer AppSettings dans Koin (androidModule + iosModule)
- [x] Mettre à jour InvoiceList.kt pour utiliser AppSettings
- [x] Mettre à jour DatabaseExportDialog.kt pour utiliser AppSettings

---

### SUJET 6 : expect/actual - PDF Generation - 🟡 PARTIEL

#### 6.1 - Interface PDF ✅
- [x] Créer `shared/commonMain/pdf/PdfGenerator.kt` avec `expect`
- [x] Créer `shared/commonMain/pdf/PdfResult.kt`

#### 6.2 - Implémentation Android (iText) 🟡
- [x] Créer `shared/androidMain/pdf/PdfGenerator.kt` (stub)
- [ ] Migrer le code de `CreatePdfWithIText.kt` vers l'actual Android
- [ ] iText reste dans app/ pour l'instant

#### 6.3 - Implémentation iOS (PDFKit) ❌
- [x] Créer `shared/iosMain/pdf/PdfGenerator.kt` (stub)
- [ ] Créer wrapper Swift pour PDFKit exposé à Kotlin
- [ ] Implémenter la génération PDF avec PDFKit
- [ ] Reproduire le même layout que la version Android

#### 6.4 - FileStorage expect/actual ✅
- [x] Créer `shared/commonMain/pdf/FileStorage.kt` avec `expect`
- [x] Créer `shared/androidMain/pdf/FileStorage.kt` (stub)
- [x] Créer `shared/iosMain/pdf/FileStorage.kt` (stub)

#### 6.5 - FontLoader expect/actual ✅
- [x] Créer `shared/commonMain/pdf/FontLoader.kt` avec `expect`
- [x] Créer `shared/androidMain/pdf/FontLoader.kt` (stub)
- [x] Créer `shared/iosMain/pdf/FontLoader.kt` (stub)

---

### SUJET 7 : Navigation Compose Multiplatform - 🟡 PARTIEL

#### 7.1 - Setup Navigation ✅
- [x] Navigation Compose disponible via androidx.navigation (Android)
- [x] Garder la structure de navigation existante

#### 7.2 - Migration Navigation 🟡
- [x] `Screen.kt` migré vers `shared/commonMain/ui/navigation/`
- [x] `Category.kt` migré vers `shared/commonMain/ui/navigation/`
- [x] `AppBarAction.kt` migré vers `shared/commonMain/ui/navigation/`
- [x] `TopBarActionView.kt` migré vers `shared/commonMain/ui/navigation/`
- [x] `NavigationComponents.kt` créé (AddIconAndLabelInColumn, ViewWithLayout)
- [x] `ButtonWithDropdownMenu.kt` migré vers `shared/commonMain/ui/navigation/`
- [x] `CategoriesDropdownMenu.kt` migré vers `shared/commonMain/ui/navigation/`
- [x] `BottomBarActionView.kt` migré vers `shared/commonMain/ui/navigation/`
- [ ] TopBar.kt reste dans app (utilise R.string et NavController spécifique Android)
- [ ] BottomBarAction.kt reste dans app (enveloppe BottomBarActionView avec BottomAppBar)
- [ ] NavGraph*.kt restent dans app (dépendent des screens/viewmodels non migrés)
- [ ] Vérifier que les arguments de navigation fonctionnent

#### 7.3 - Vérification
- [ ] Tester toutes les navigations Android
- [ ] Vérifier les animations de transition

---

### SUJET 8 : UI Compose Multiplatform - 🟡 PARTIEL

#### 8.1 - Setup Compose Multiplatform ✅
- [x] Plugin `org.jetbrains.compose` dans shared/build.gradle.kts
- [x] Configurer Compose pour iOS
- [x] Dépendances compose-multiplatform ajoutées

#### 8.2 - Migration du Theme ✅
- [x] Déplacer `ui/theme/Theme.kt` vers `shared/commonMain/ui/theme/`
- [x] Déplacer `ui/theme/Color.kt`
- [x] Déplacer `ui/theme/Typography.kt`

#### 8.3 - Migration des Strings ✅
- [x] Créer `shared/commonMain/composeResources/values/strings.xml`
- [x] Migrer les strings depuis `app/res/values/strings.xml`
- [x] Créer traductions FR dans strings.xml (attribut xml:lang)
- [x] Remplacer `stringResource(R.string.xxx)` par `stringResource(Res.string.xxx)`
- [x] Garder `Strings.get()` pour strings dynamiques (Android R.string)

#### 8.4 - Migration des Composants Partagés 🟡
**Migrés vers shared :**
- [x] `Separators.kt`
- [x] `FlippyCheckBox.kt`
- [x] `ButtonAddOrChoose.kt`
- [x] `FormInputDefaultStyle.kt`
- [x] `DecimalInputVisualTransformation.kt`
- [x] `BatAnimation.kt` (Compottie)
- [x] `FormUI.kt` (data classes + composables: FormUI, PageElementCreator, RowWithLabelAndInput)
- [x] `DecimalFormatter.kt`
- [x] `FormInputCreatorGoForward.kt`
- [x] `FormInputCreatorListPicker.kt`
- [x] `FormInputCreatorText.kt`
- [x] `FormInputCreatorDecimal.kt`
- [x] `FormInputCreatorDoublePrice.kt`
- [x] `AlertDialogDeleteDocument.kt`
- [x] `AlertDialogErrorOrInfo.kt`
- [x] `WhatsNewDialog.kt`
- [x] `GeneralBottomBar.kt`

**ui/navigation migrés:**
- [x] `BottomBarAction.kt`
- [x] `DocumentBottomBar.kt`
- [x] `DocumentBottomBarView.kt`

**ui/screens/shared migrés:**
- [x] `DocumentBottomSheetTypeOfForm.kt`
- [x] `ScaffoldWithDimmedOverlay.kt`
- [x] `DocumentBottomSheetLargeText.kt`

**Restent dans app (à migrer ou garder Android-specific) :**
- [ ] `FormInputCreatorDate.kt` (commenté/non utilisé)
- [ ] `DocumentBottomSheet*.kt` (nombreux fichiers liés)
- [ ] `DocumentBasicTemplate*.kt` (template PDF)
- [ ] `SwipeBackground.kt` (commenté/non utilisé)
- [ ] `pullrefresh/` (custom implementation)

**Android-specific (restent dans app) :**
- [x] `CreatePdfWithIText.kt` (iText7 - Android only)
- [x] `FormInputsValidator.kt` (android.util.Patterns)
- [x] `KeyboardVisibility.kt` (Android specific)
- [x] `PdfUtils.kt`

#### 8.5 - Migration des Screens ❌
- [ ] Déplacer `ui/screens/InvoiceList.kt` vers `shared/commonMain/ui/screens/`
- [ ] Déplacer `ui/screens/InvoiceAddEdit.kt`
- [ ] Déplacer `ui/screens/DeliveryNoteList.kt`
- [ ] Déplacer `ui/screens/DeliveryNoteAddEdit.kt`
- [ ] Déplacer `ui/screens/CreditNoteList.kt`
- [ ] Déplacer `ui/screens/CreditNoteAddEdit.kt`
- [ ] Déplacer `ui/screens/ProductList.kt`
- [ ] Déplacer `ui/screens/ProductAddEdit.kt`
- [ ] Déplacer `ui/screens/ClientOrIssuerList.kt`
- [ ] Déplacer `ui/screens/ClientAddEdit.kt`
- [ ] Déplacer `ui/screens/Settings.kt`
- [ ] Déplacer `ui/screens/About.kt`
- [ ] Déplacer `ui/screens/Account.kt`
- [ ] Déplacer `ui/screens/ExportPdf.kt`
- [ ] Déplacer tous les fichiers `ui/screens/shared/`

#### 8.6 - Migration des ViewModels ✅ TERMINÉ
**ListViewModels migrés (13 Jan 2026):**
- [x] `InvoiceListViewModel` → `shared/commonMain/ui/viewmodels/`
- [x] `DeliveryNoteListViewModel` → `shared/commonMain/ui/viewmodels/`
- [x] `CreditNoteListViewModel` → `shared/commonMain/ui/viewmodels/`
- [x] `ProductListViewModel` → `shared/commonMain/ui/viewmodels/`
- [x] `ClientOrIssuerListViewModel` → `shared/commonMain/ui/viewmodels/`
- [x] Ajout `koin-compose:1.1.5` et `koin-compose-viewmodel:1.2.0-Beta5`
- [x] Ajout `lifecycle-viewmodel-compose:2.8.2`

**AddEditViewModels migrés (13 Jan 2026):**
- [x] `InvoiceAddEditViewModel` → `shared/commonMain/ui/viewmodels/`
- [x] `DeliveryNoteAddEditViewModel` → `shared/commonMain/ui/viewmodels/`
- [x] `CreditNoteAddEditViewModel` → `shared/commonMain/ui/viewmodels/`
- [x] `ProductAddEditViewModel` → `shared/commonMain/ui/viewmodels/`
- [x] `ClientOrIssuerAddEditViewModel` → `shared/commonMain/ui/viewmodels/`
- [x] `FormInputsValidator` → `shared/commonMain/ui/shared/`

**Encore dans app/ (Android-specific):**
- [ ] `AlertDialogViewModel` (utilise DataStore Android)
- [ ] `AccountViewModel` (placeholder)

**Adaptations effectuées:**
- `SavedStateHandle` → paramètres `itemId` et `type` passés via Koin `parametersOf`
- `localizedMessage` → `message` (KMP compatible)
- `android.util.Patterns.EMAIL_ADDRESS` → regex Kotlin pure
- Strings Android (R.string) → paramètres dans fonctions ViewModel
- `collectAsStateWithLifecycle` gardé côté Android (NavGraph)

#### 8.7 - Adaptation androidApp ❌
- [ ] Simplifier `MainActivity.kt` pour juste appeler le composable root de shared
- [ ] Retirer le code UI dupliqué de app

#### 8.8 - Vérification iOS ❌
- [ ] Lancer l'app iOS dans le simulateur
- [ ] Vérifier l'affichage de tous les écrans
- [ ] Vérifier les interactions (tap, scroll, navigation)
- [ ] Tester la génération PDF
- [ ] Vérifier les animations Compottie

---

### SUJET 9 : Tests et Finalisation - ❌ NON COMMENCÉ

#### 9.1 - Tests unitaires
- [ ] Ajouter tests pour les fonctions utilitaires partagées
- [ ] Ajouter tests pour les DataSources

#### 9.2 - Tests d'intégration
- [ ] Tester le flow complet facture Android
- [ ] Tester le flow complet facture iOS
- [ ] Tester export PDF Android
- [ ] Tester export PDF iOS

#### 9.3 - Nettoyage final
- [ ] Supprimer le code mort
- [ ] Vérifier les imports inutilisés
- [ ] Mettre à jour le README
- [ ] Créer documentation KMP

---

## Décisions Prises

1. **Auth/Retrofit** : SUPPRIMÉ (pas utilisé)
2. **Lottie** : Remplacé par **Compottie** (lib KMP pour Lottie)
3. **Navigation** : Utiliser `navigation-compose` de JetBrains Compose Multiplatform (pas Voyager)
4. **ViewModels** : Garder les ViewModels avec `koin-compose-viewmodel` (pas de ScreenModel)
5. **Compose Multiplatform** : Version 1.7.0+ (stable avec navigation)
6. **Strings** : Migrer vers `composeResources/values/` pour multiplatform
7. **PDF iOS** : Wrapper Swift appelant PDFKit, exposé à Kotlin via expect/actual
8. **BigDecimal** : Utiliser `com.ionspin.kotlin:bignum` (KMP) au lieu de java.math.BigDecimal
9. **Structure** : Garder `app/` au lieu de renommer en `androidApp/` (fonctionne)

---

## Ce que je NE modifierai PAS sans demander

- Logique de calcul des prix
- Logique de génération des numéros de documents
- Structure des données (InvoiceState, ProductState, etc.)
- Comportement des ViewModels
- Requêtes SQLDelight
- Format du PDF généré

Toute modification de logique métier sera soumise à validation avant implémentation.
