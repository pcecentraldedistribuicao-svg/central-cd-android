# Central CD 10&CIA - Android Shell 1.0.3 CORRIGIDO

Projeto Android WebView configurado para abrir diretamente a Central CD publicada em:

https://script.google.com/macros/s/AKfycbx_M5toGyI4iSg1Ox1Pur9mJySUXN-bN3FOXKS7mrG5WQscqAfy9I5FSsra1mCdvTE/exec

## Correcoes desta versao
- AndroidManifest.xml com `10&amp;CIA` (XML valido).
- `URLUtil` importado de `android.webkit.URLUtil`.
- Dialogo de configuracao corrigido: `setOnShowListener` aplicado ao `AlertDialog`, nao ao `Builder`.
- Workflow do GitHub Actions incluido em `.github/workflows/build-apk.yml`.
- Versao Android 1.0.3 / versionCode 4.

## Estrutura correta no repositorio
- `.github/workflows/build-apk.yml`
- `app/...`
- `build.gradle`
- `gradle.properties`
- `settings.gradle`

## Build
Ao enviar o conteudo deste projeto para a branch `main`, o GitHub Actions executa `Build Central CD APK`.
Quando ficar verde, abra a execucao e baixe o artefato `Central-CD-APK-1.0.3`.
Dentro do ZIP do artefato estara `app-debug.apk`.
