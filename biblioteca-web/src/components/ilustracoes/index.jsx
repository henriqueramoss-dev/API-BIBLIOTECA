
import Svg, { Circle, Ellipse, G, Line, Path, Polygon, Rect } from "./svg"

// Ilustrações de biblioteca desenhadas em SVG (estante, pilha de livros, livro aberto, capa e faixa de lombadas).
// O mesmo arquivo serve ao site e ao app: só a linha de import acima muda.

// Cores das lombadas (tons de couro e tecido) e da madeira das estantes.
const LOMBADAS = ["#8C2F39", "#2F5D50", "#2D4A6B", "#C99A2E", "#B5593C", "#5E3A5C", "#3E7C7B", "#6B4B2A"]
const MADEIRA = "#7A4E2A"
const MADEIRA_CLARA = "#9A6A3C"
const MADEIRA_ESCURA = "#4A2E18"
const PAPEL = "#F8EFD8"
const PAPEL_SOMBRA = "#E3D3AE"
const OURO = "#D9AE5B"
const TINTA = "#6A5444"

// Atributos que escondem a ilustração de leitores de tela (ela é só decoração).
const decorativo = {
    accessible: false,
    importantForAccessibility: "no-hide-descendants",
    "aria-hidden": true,
    focusable: false
}


// Gira um retângulo em torno do canto inferior esquerdo (para o livro inclinado da estante).
const girar = (x, y, w, h, graus) => {

    const rad = (graus * Math.PI) / 180
    const ox = x
    const oy = y + h

    const cantos = [[x, y], [x + w, y], [x + w, y + h], [x, y + h]]

    return cantos
        .map(([px, py]) => {
            const dx = px - ox
            const dy = py - oy
            return `${(ox + dx * Math.cos(rad) - dy * Math.sin(rad)).toFixed(1)},${(oy + dx * Math.sin(rad) + dy * Math.cos(rad)).toFixed(1)}`
        })
        .join(" ")
}


// Escolhe sempre a mesma cor de capa para a mesma chave (id ou título do livro).
const corDaCapa = (chave) => {

    const texto = String(chave ?? "")

    let soma = 0
    for (let i = 0; i < texto.length; i++) soma = (soma * 31 + texto.charCodeAt(i)) % 997

    return LOMBADAS[soma % LOMBADAS.length]
}


// UM LIVRO EM PÉ NA ESTANTE: corpo, faixas douradas e etiqueta
const LivroEmPe = ({ x, largura, altura, cor, base, estilo = 0 }) => {

    const topo = base - altura

    return (
        <G>
            <Rect x={x} y={topo} width={largura} height={altura} rx={2.5} fill={cor} />
            <Rect x={x + 2} y={topo} width={2.5} height={altura} fill="#FFFFFF" opacity={0.14} />
            <Rect x={x} y={topo + 8} width={largura} height={2.5} fill={OURO} opacity={0.9} />
            <Rect x={x} y={base - 12} width={largura} height={2.5} fill={OURO} opacity={0.9} />

            {estilo === 1 && (
                <Rect x={x + largura * 0.2} y={topo + altura * 0.35} width={largura * 0.6} height={altura * 0.22} rx={1.5} fill={PAPEL} opacity={0.92} />
            )}

            {estilo === 2 && (
                <Circle cx={x + largura / 2} cy={topo + altura * 0.48} r={Math.min(largura * 0.22, 5)} fill="none" stroke={OURO} strokeWidth={1.6} />
            )}

            {estilo === 3 && (
                <>
                    <Rect x={x} y={topo + altura * 0.4} width={largura} height={1.8} fill={OURO} opacity={0.9} />
                    <Rect x={x} y={topo + altura * 0.4 + 5} width={largura} height={1.8} fill={OURO} opacity={0.9} />
                </>
            )}
        </G>
    )
}


// ESTANTE COM LIVROS, UMA PLANTINHA E UM APOIO DE LIVROS (ilustração principal)
const Estante = ({ width = 320, height = (width * 170) / 320 }) => {

    const base = 140

    const livros = [
        [34, 17, 92, 0, 1], [53, 13, 108, 1, 0], [68, 19, 84, 2, 2], [89, 15, 112, 3, 3],
        [106, 21, 96, 4, 1], [129, 14, 104, 5, 0], [145, 18, 80, 6, 2]
    ]

    return (
        <Svg width={width} height={height} viewBox="0 0 320 170" {...decorativo}>

            <Ellipse cx={160} cy={160} rx={146} ry={6} fill={MADEIRA_ESCURA} opacity={0.18} />

            {/* apoio de livros (esquerda) */}
            <Path d="M14 140 L14 82 Q14 78 18 78 L24 78 L24 132 L34 132 L34 140 Z" fill="#8A8F98" />
            <Path d="M14 140 L14 82 Q14 78 18 78 L19.5 78 L19.5 140 Z" fill="#FFFFFF" opacity={0.25} />

            {livros.map(([x, l, a, c, e]) => (
                <LivroEmPe key={`a${x}`} x={x} largura={l} altura={a} cor={LOMBADAS[c]} base={base} estilo={e} />
            ))}

            {/* livro inclinado, encostado no último */}
            <Polygon points={girar(166, base - 98, 17, 98, 16)} fill="#2D4A6B" />

            {/* livros deitados sobre a estante */}
            <Rect x={196} y={base - 13} width={74} height={13} rx={2} fill="#B5593C" />
            <Rect x={196} y={base - 13} width={74} height={3} rx={1.5} fill={OURO} opacity={0.9} />
            <Rect x={200} y={base - 26} width={64} height={13} rx={2} fill="#2F5D50" />
            <Rect x={200} y={base - 26} width={64} height={3} rx={1.5} fill={OURO} opacity={0.9} />
            <Rect x={206} y={base - 38} width={50} height={12} rx={2} fill={PAPEL} />
            <Rect x={206} y={base - 38} width={50} height={3} rx={1.5} fill={OURO} opacity={0.9} />

            {/* planta */}
            <Path d="M284 140 L288 118 L308 118 L312 140 Z" fill="#B5593C" />
            <Rect x={286} y={115} width={24} height={5} rx={2} fill="#C8694A" />
            <Path d="M298 116 Q286 98 280 88 Q296 90 298 116 Z" fill="#4E8A5B" />
            <Path d="M298 116 Q310 96 318 90 Q302 88 298 116 Z" fill="#3E7A4C" />
            <Path d="M298 116 Q296 92 298 74 Q304 92 298 116 Z" fill="#5E9C6A" />

            {/* tábua da estante */}
            <Rect x={6} y={base} width={308} height={16} rx={3} fill={MADEIRA} />
            <Rect x={6} y={base} width={308} height={4} rx={2} fill={MADEIRA_CLARA} />
            <Rect x={6} y={base + 13} width={308} height={3} rx={1.5} fill={MADEIRA_ESCURA} opacity={0.5} />

        </Svg>
    )
}


// PILHA DE LIVROS DEITADOS COM UM MARCADOR DE PÁGINA (usada nas listas vazias)
const PilhaDeLivros = ({ width = 200, height = (width * 150) / 200 }) => {

    const livro = (x, y, l, cor, extra) => (
        <G key={`${x}-${y}`}>
            <Rect x={x} y={y} width={l} height={22} rx={4} fill={cor} />
            <Rect x={x + 8} y={y + 4} width={l - 14} height={14} rx={2} fill={PAPEL} />
            <Rect x={x + 8} y={y + 8} width={l - 20} height={1.5} fill={PAPEL_SOMBRA} />
            <Rect x={x + 8} y={y + 12} width={l - 26} height={1.5} fill={PAPEL_SOMBRA} />
            <Rect x={x} y={y} width={9} height={22} rx={4} fill="#000000" opacity={0.2} />
            <Rect x={x} y={y + 6} width={l} height={2} fill={OURO} opacity={0} />
            {extra}
        </G>
    )

    return (
        <Svg width={width} height={height} viewBox="0 0 200 150" {...decorativo}>

            <Ellipse cx={100} cy={136} rx={78} ry={7} fill={MADEIRA_ESCURA} opacity={0.18} />

            {livro(34, 100, 132, "#2F5D50")}
            {livro(46, 76, 118, "#8C2F39")}
            {livro(30, 52, 126, "#C99A2E",
                <Path d="M140 74 L140 98 L145 92 L150 98 L150 74 Z" fill="#8C2F39" />
            )}

            {/* livro menor no topo */}
            {livro(50, 28, 100, "#2D4A6B")}

            {/* brilhos */}
            <Path d="M168 40 L170 46 L176 48 L170 50 L168 56 L166 50 L160 48 L166 46 Z" fill={OURO} />
            <Path d="M24 70 L25.5 74 L30 75.5 L25.5 77 L24 81 L22.5 77 L18 75.5 L22.5 74 Z" fill={OURO} opacity={0.8} />

        </Svg>
    )
}


// LIVRO ABERTO COM PÁGINAS, TEXTO E MARCADOR (telas de entrada)
const LivroAberto = ({ width = 240, height = (width * 150) / 240 }) => {

    const linhasEsq = [48, 60, 72, 84, 96]
    const linhasDir = [48, 60, 72, 84, 96]

    return (
        <Svg width={width} height={height} viewBox="0 0 240 150" {...decorativo}>

            <Ellipse cx={120} cy={136} rx={96} ry={7} fill={MADEIRA_ESCURA} opacity={0.18} />

            {/* capa */}
            <Path d="M12 108 Q12 124 28 126 L120 132 L212 126 Q228 124 228 108 L228 100 L12 100 Z" fill="#8C2F39" />
            <Path d="M12 112 L120 122 L228 112" stroke={OURO} strokeWidth={1.5} fill="none" opacity={0.85} />

            {/* páginas empilhadas (bordas) */}
            <Path d="M20 100 L20 104 L120 114 L220 104 L220 100 Z" fill={PAPEL_SOMBRA} />

            {/* página esquerda */}
            <Path d="M20 96 L20 32 Q70 22 120 40 L120 112 Q70 94 20 96 Z" fill={PAPEL} stroke={PAPEL_SOMBRA} strokeWidth={1.5} strokeLinejoin="round" />
            {/* página direita */}
            <Path d="M220 96 L220 32 Q170 22 120 40 L120 112 Q170 94 220 96 Z" fill={PAPEL} stroke={PAPEL_SOMBRA} strokeWidth={1.5} strokeLinejoin="round" />

            {/* linhas de texto */}
            {linhasEsq.map((y, i) => (
                <Line key={`e${y}`} x1={34} y1={y - 8 + i * 0.6} x2={i === 4 ? 70 : 104} y2={y + 4 + i * 0.6 + 4} stroke={TINTA} strokeWidth={2.4} strokeLinecap="round" opacity={0.55} />
            ))}
            {linhasDir.map((y, i) => (
                <Line key={`d${y}`} x1={136} y1={y + 4 + i * 0.6 + 4 - 12} x2={i === 4 ? 172 : 206} y2={y - 8 + i * 0.6} stroke={TINTA} strokeWidth={2.4} strokeLinecap="round" opacity={0.55} />
            ))}

            {/* calha central */}
            <Path d="M120 40 L120 112" stroke={PAPEL_SOMBRA} strokeWidth={2.5} />

            {/* marcador de página */}
            <Path d="M150 33 L150 78 L156 71 L162 78 L162 31 Z" fill="#C99A2E" />

            {/* brilhos */}
            <Path d="M120 8 L122.5 15 L130 17.5 L122.5 20 L120 27 L117.5 20 L110 17.5 L117.5 15 Z" fill={OURO} />
            <Path d="M186 14 L187.6 18.4 L192 20 L187.6 21.6 L186 26 L184.4 21.6 L180 20 L184.4 18.4 Z" fill={OURO} opacity={0.8} />
            <Path d="M54 16 L55.6 20.4 L60 22 L55.6 23.6 L54 28 L52.4 23.6 L48 22 L52.4 20.4 Z" fill={OURO} opacity={0.8} />

        </Svg>
    )
}


// CAPA DE LIVRO ILUSTRADA: a cor muda de livro para livro (veja corDaCapa)
const CapaDeLivro = ({ cor = "#8C2F39", width = 60, height = (width * 84) / 60 }) => {

    return (
        <Svg width={width} height={height} viewBox="0 0 60 84" {...decorativo}>

            <Rect x={4} y={4} width={54} height={78} rx={4} fill={MADEIRA_ESCURA} opacity={0.18} />
            <Rect x={0} y={0} width={56} height={80} rx={4} fill={cor} />
            <Rect x={0} y={0} width={9} height={80} rx={4} fill="#000000" opacity={0.2} />
            <Rect x={8} y={0} width={1.4} height={80} fill="#FFFFFF" opacity={0.2} />
            <Rect x={52} y={4} width={4} height={72} fill={PAPEL} />

            <Rect x={15} y={8} width={34} height={64} rx={2} fill="none" stroke={OURO} strokeWidth={1.4} />

            <Circle cx={32} cy={30} r={8} fill="none" stroke={OURO} strokeWidth={1.6} />
            <Path d="M32 24 L33.8 28.8 L39 29 L34.9 32.2 L36.3 37 L32 34.2 L27.7 37 L29.1 32.2 L25 29 L30.2 28.8 Z" fill={OURO} />

            <Line x1={22} y1={50} x2={42} y2={50} stroke={OURO} strokeWidth={2} strokeLinecap="round" />
            <Line x1={25} y1={57} x2={39} y2={57} stroke={OURO} strokeWidth={2} strokeLinecap="round" opacity={0.8} />

        </Svg>
    )
}


// FAIXA DE LOMBADAS: fila de livros em pé, que se estica para ocupar a largura (decoração de topo)
const FaixaDeLombadas = ({ height = 44 }) => {

    const larguras = [14, 11, 16, 12, 18, 13, 15, 11, 17, 12, 14, 19, 12, 15, 11, 16, 13, 18, 12, 14, 16, 11, 15, 13, 17, 12, 19, 14, 11, 16, 13, 15, 12, 18, 14, 11, 16, 13]
    const alturas = [30, 34, 26, 36, 28, 33, 24, 35, 29, 32, 27, 36, 25, 34, 30, 28, 35, 26, 33, 29, 36, 27, 31, 24, 34, 30, 28, 35, 26, 32, 29, 34, 25, 36, 30, 27, 33, 28]

    let x = 0

    return (
        <Svg width="100%" height={height} viewBox="0 0 400 44" preserveAspectRatio="xMidYMax slice" {...decorativo}>

            {larguras.map((l, i) => {

                const livroX = x
                x += l + 1

                return (
                    <G key={i}>
                        <Rect x={livroX} y={40 - alturas[i]} width={l} height={alturas[i]} rx={1.8} fill={LOMBADAS[i % LOMBADAS.length]} />
                        <Rect x={livroX} y={40 - alturas[i] + 5} width={l} height={1.8} fill={OURO} opacity={0.9} />
                        <Rect x={livroX} y={32} width={l} height={1.8} fill={OURO} opacity={0.9} />
                    </G>
                )
            })}

            <Rect x={0} y={39} width={400} height={5} fill={MADEIRA} />
            <Rect x={0} y={39} width={400} height={1.6} fill={MADEIRA_CLARA} />

        </Svg>
    )
}


export {
    LOMBADAS,
    corDaCapa,
    Estante,
    PilhaDeLivros,
    LivroAberto,
    CapaDeLivro,
    FaixaDeLombadas
}
