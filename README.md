# Central CD 10&CIA - Android Shell 1.0.4

Aplicativo Android WebView da Central CD 10&CIA.

## Atualizações

- **Central Web (Code.gs/HTML):** atualiza automaticamente no aplicativo sempre que a mesma implantação `/exec` é atualizada.
- **Aplicativo Android:** só precisa nova versão quando houver alteração nativa (ícone, permissões Android, WebView, downloads etc.). A versão 1.0.4 verifica a última GitHub Release e avisa quando houver APK nativo mais novo.

## Ícone

A versão 1.0.4 inclui ícone próprio amarelo/azul da Central CD.

## Build rápido

`Build Central CD APK` gera um APK debug para teste.

## Release oficial assinada

`Publish Signed Central CD APK` gera APK release com assinatura permanente e publica em GitHub Releases. Antes, configure os secrets descritos em `release_setup/GITHUB_SECRETS.txt`.

**Nunca publique a pasta `release_setup` no repositório público.** Ela contém a chave de assinatura e dados secretos.
