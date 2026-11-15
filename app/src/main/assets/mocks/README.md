# Mocks

Respostas usadas pelo flavor **`mock`** (Build Variants → `mockDebug`). O `MockInterceptor` responde
cada requisição com um arquivo daqui, e a resposta passa pelo mesmo Retrofit + kotlinx.serialization
da API real.

- Caminho: `{MÉTODO}/{endpoint}.json` — ex.: `GET/products.json` responde `GET /products`.
- `_` casa qualquer segmento: `GET/products/sku/_.json` responde qualquer SKU sem arquivo próprio.
  Um arquivo exato (`GET/products/sku/LUNA.json`) sempre vence o curinga.
- Status e latência: arquivo irmão `{nome}.meta.json` com `{ "status": 404, "delayMs": 1500 }`
  (padrão: 200 e 400 ms).
- Endpoint sem mock responde 404 com a mensagem `No mock for …`.
- Todo arquivo novo precisa ser registrado em `MockFixturesTest`, que o decodifica com os modelos
  reais — um mock fora do contrato quebra o CI.

O login aceita qualquer email/senha e o perfil mockado é **admin**, para exercitar a aba Admin.
