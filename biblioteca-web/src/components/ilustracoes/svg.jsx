
// Troca as peças do react-native-svg por elementos SVG comuns do navegador.
// Assim o arquivo de ilustrações do site é igual ao do app.

// (accessible e importantForAccessibility existem só no React Native: são descartados aqui)
const Svg = ({ accessible, importantForAccessibility, ...resto }) => <svg xmlns="http://www.w3.org/2000/svg" {...resto} />
const G = (props) => <g {...props} />
const Rect = (props) => <rect {...props} />
const Path = (props) => <path {...props} />
const Circle = (props) => <circle {...props} />
const Ellipse = (props) => <ellipse {...props} />
const Line = (props) => <line {...props} />
const Polygon = (props) => <polygon {...props} />


export default Svg

export {
    Svg,
    G,
    Rect,
    Path,
    Circle,
    Ellipse,
    Line,
    Polygon
}
