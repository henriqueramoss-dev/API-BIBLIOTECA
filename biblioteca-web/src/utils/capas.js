
// Capas de livros de verdade, buscadas pelo ISBN no Open Library (https://openlibrary.org).
// O endereço pede ao serviço só a imagem: se o livro não tiver capa lá (ou se estiver sem internet),
// o app usa a capa ilustrada no lugar.

const SERVIDOR_DE_CAPAS = "https://covers.openlibrary.org/b/isbn"


// Deixa só os dígitos (e o X final do ISBN-10). Devolve null se não parecer um ISBN.
const limparIsbn = (isbn) => {

    const limpo = String(isbn ?? "").replace(/[^0-9Xx]/g, "").toUpperCase()

    return limpo.length === 10 || limpo.length === 13 ? limpo : null
}


// Endereço da imagem da capa. Tamanhos: "S" (pequena), "M" (média) e "L" (grande).
// default=false faz o serviço responder 404 (em vez de uma imagem em branco) quando não há capa.
const urlDaCapa = (isbn, tamanho = "M") => {

    const limpo = limparIsbn(isbn)

    return limpo ? `${SERVIDOR_DE_CAPAS}/${limpo}-${tamanho}.jpg?default=false` : null
}


export {
    limparIsbn,
    urlDaCapa
}
