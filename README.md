# Central CD 10&CIA - Android Shell 1.0.2 CONFIGURADO

Este projeto ja esta configurado para abrir diretamente a Central CD publicada em:

https://script.google.com/macros/s/AKfycbx_M5toGyI4iSg1Ox1Pur9mJySUXN-bN3FOXKS7mrG5WQscqAfy9I5FSsra1mCdvTE/exec

Nao e necessario informar CENTRAL_URL para o primeiro build. O secret do GitHub continua opcional e serve apenas para substituir a URL no futuro sem editar o projeto.

## Gerar o APK pelo GitHub Actions

1. Crie um repositorio novo no GitHub, por exemplo `central-cd-android`.
2. Extraia este ZIP e envie TODO o conteudo para a raiz do repositorio, inclusive a pasta `.github`.
3. Abra a aba **Actions** no GitHub.
4. Se o GitHub solicitar, clique em **I understand my workflows, go ahead and enable them**.
5. Abra **Build Central CD APK**.
6. Clique em **Run workflow** e confirme.
7. Aguarde a execucao ficar verde.
8. Abra a execucao concluida e, em **Artifacts**, baixe **Central-CD-APK**.
9. Extraia o arquivo baixado. Dentro dele estara `app-debug.apk`.
10. Envie o APK ao celular e instale.

## Atualizacoes da Central

Alteracoes normais no Code.gs e Index.html nao exigem gerar outro APK, desde que o link /exec da implantacao continue o mesmo. O app sempre abre a Central web publicada.

## Quando gerar outro APK

Somente quando mudar algo nativo: nome do app, icone, permissoes Android, integracoes nativas, package ID ou se desejar publicar uma versao Android assinada/release.

## APK dentro da propria Central

Depois do primeiro build, hospede o APK em HTTPS (por exemplo GitHub Release ou Drive) e informe o link em **Central de Aplicacoes > Aplicativo Android > Configurar APK**. A Central exibira o botao de download para os usuarios.
