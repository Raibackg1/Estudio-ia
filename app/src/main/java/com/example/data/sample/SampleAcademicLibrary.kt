package com.example.data.sample

data class AcademicDocument(
    val id: String,
    val title: String,
    val subject: String,
    val author: String,
    val totalPages: Int,
    val summary: String,
    val iconName: String,
    val pages: List<AcademicPage>
)

data class AcademicPage(
    val pageNumber: Int,
    val chapterTitle: String,
    val contentText: String,
    val defaultHighlights: List<String> = emptyList(),
    val keyTerms: List<String> = emptyList(),
    val reviewQuestions: List<String> = emptyList()
)

object SampleAcademicLibrary {
    val documents = listOf(
        AcademicDocument(
            id = "doc_fisica",
            title = "Fundamentos de Física y Mecánica Cuántica",
            subject = "Física",
            author = "Dr. Alejandro Valdés",
            totalPages = 4,
            summary = "Tratado completo sobre relatividad, termodinámica, mecánica analítica y principios cuánticos.",
            iconName = "science",
            pages = listOf(
                AcademicPage(
                    pageNumber = 1,
                    chapterTitle = "Capítulo 1: La Dualidad Onda-Corpúsculo y Principio de Incertidumbre",
                    contentText = """
                    La física del siglo XX experimentó una revolución con el descubrimiento de la naturaleza cuántica de la radiación electromagnética. Max Planck postuló en 1900 que los osciladores atómicos emiten energía en paquetes discretos llamados cuantos, expresados mediante E = h·ν, donde h es la constante de Planck (6.626 × 10⁻³⁴ J·s).

                    Posteriormente, Louis de Broglie propuso que toda partícula con masa m y velocidad v posee una longitud de onda asociada λ = h / p. Este fenómeno fue corroborado experimentalmente por Davisson y Germer al observar la difracción de electrones en redes cristalinas de níquel.

                    El Principio de Incertidumbre de Werner Heisenberg (1927) demostró que es físicamente imposible determinar de manera simultánea y con precisión arbitraria la posición x y el momento lineal p de una partícula subatómica: Δx · Δp ≥ ℏ / 2, donde ℏ = h / (2π). Esta indeterminación no es una deficiencia instrumental, sino una propiedad intrínseca de la naturaleza ondulatoria de la materia.
                    """.trimIndent(),
                    defaultHighlights = listOf(
                        "E = h·ν, donde h es la constante de Planck",
                        "longitud de onda asociada λ = h / p",
                        "Δx · Δp ≥ ℏ / 2"
                    ),
                    keyTerms = listOf("Cuanto de Energía", "Dualidad Onda-Partícula", "Constante de Planck", "Incertidumbre de Heisenberg"),
                    reviewQuestions = listOf(
                        "¿Cómo dedujo De Broglie la longitud de onda asociada a un electrón?",
                        "¿Por qué la constante de Planck impone un límite cuántico a la precisión?",
                        "Explica el experimento de difracción de electrones."
                    )
                ),
                AcademicPage(
                    pageNumber = 2,
                    chapterTitle = "Capítulo 2: Termodinámica Estadística y Entropía Universal",
                    contentText = """
                    La termodinámica clásica describe las transformaciones de calor en trabajo mecánico mediante cuatro postulados fundamentales. La Ley Cero establece el principio de equilibrio térmico transitivo.

                    La Primera Ley postula la conservación de la energía: ΔU = Q - W, indicando que el cambio en la energía interna de un sistema termodinámico cerrado equivale al calor neto absorbido menos el trabajo realizado sobre su entorno.

                    La Segunda Ley introduce el concepto de entropía (S), definido por Rudolf Clausius como dS = dQ_rev / T y generalizado por Ludwig Boltzmann en la termodinámica estadística como S = k_B · ln(Ω), donde Ω representa el número de microestados accesibles para un macroestado dado. En cualquier proceso espontáneo en un sistema aislado, la entropía total del universo siempre aumenta (ΔS_universo > 0).
                    """.trimIndent(),
                    defaultHighlights = listOf(
                        "ΔU = Q - W",
                        "S = k_B · ln(Ω)",
                        "la entropía total del universo siempre aumenta (ΔS_universo > 0)"
                    ),
                    keyTerms = listOf("Entropía de Boltzmann", "Microestados", "Conservación de Energía", "Equilibrio Térmico"),
                    reviewQuestions = listOf(
                        "¿Qué diferencia existe entre un proceso reversible e irreversible?",
                        "¿Cómo se relaciona la fórmula de Boltzmann con el desorden molecular?",
                        "Aplica la primera ley a una expansión isotérmica de gas ideal."
                    )
                ),
                AcademicPage(
                    pageNumber = 3,
                    chapterTitle = "Capítulo 3: Relatividad Especial y Dilatación Temporal",
                    contentText = """
                    Albert Einstein formuló en 1905 la Teoría de la Relatividad Especial basándose en dos postulados: las leyes de la física son idénticas en todos los marcos de referencia inerciales, y la velocidad de la luz en el vacío (c ≈ 3 × 10⁸ m/s) es invariante para cualquier observador sin importar su movimiento relativo.

                    De estos postulados surgen las Transformaciones de Lorentz, que reemplazan a las transformaciones galileanas clásicas. Cuando un cuerpo se desplaza a una velocidad v cercana a c, el tiempo para un observador estacionario se dilata según: Δt = Δt₀ / √(1 - v²/c²).

                    Simultáneamente, la longitud en la dirección del movimiento se contrae (L = L₀ · √(1 - v²/c²)) y la equivalencia masa-energía se consagra en la celebérrima ecuación E = γ·m·c².
                    """.trimIndent(),
                    defaultHighlights = listOf(
                        "la velocidad de la luz en el vacío es invariante",
                        "Δt = Δt₀ / √(1 - v²/c²)",
                        "equivalencia masa-energía E = γ·m·c²"
                    ),
                    keyTerms = listOf("Transformaciones de Lorentz", "Dilatación del Tiempo", "Contracción de Lorentz", "Factor Gamma"),
                    reviewQuestions = listOf(
                        "¿Por qué un objeto con masa no puede superar la velocidad de la luz?",
                        "Describe la paradoja de los gemelos y su resolución relativista.",
                        "Calcula el factor γ para v = 0.8c."
                    )
                ),
                AcademicPage(
                    pageNumber = 4,
                    chapterTitle = "Capítulo 4: Electromagnetismo y Ecuaciones de Maxwell",
                    contentText = """
                    James Clerk Maxwell unificó la electricidad y el magnetismo en cuatro elegantes ecuaciones diferenciales parciales que predicen la existencia de ondas electromagnéticas auto-propagadas a la velocidad de la luz.

                    1. Ley de Gauss para la electricidad: ∇·E = ρ / ε₀ (las cargas eléctricas son fuentes del campo eléctrico).
                    2. Ley de Gauss para el magnetismo: ∇·B = 0 (no existen monopolos magnéticos aislados en la naturaleza).
                    3. Ley de Faraday-Lenz: ∇×E = -∂B/∂t (un campo magnético variable induce un campo eléctrico rotacional).
                    4. Ley de Ampère-Maxwell: ∇×B = μ₀·J + μ₀·ε₀·(∂E/∂t) (la corriente de desplazamiento genera campos magnéticos).
                    """.trimIndent(),
                    defaultHighlights = listOf(
                        "∇·E = ρ / ε₀",
                        "∇·B = 0 (no existen monopolos magnéticos)",
                        "∇×E = -∂B/∂t"
                    ),
                    keyTerms = listOf("Ecuaciones de Maxwell", "Corriente de Desplazamiento", "Ley de Faraday", "Flujo Magnético"),
                    reviewQuestions = listOf(
                        "¿Qué término añadió Maxwell a la ley de Ampère y cuál fue su impacto?",
                        "¿Cómo demuestran estas ecuaciones que la luz es una onda electromagnética?"
                    )
                )
            )
        ),
        AcademicDocument(
            id = "doc_biologia",
            title = "Biología Celular y Genética Molecular",
            subject = "Biología",
            author = "Dra. Elena Rostova",
            totalPages = 3,
            summary = "Estructura del ADN, replicación, transcripción, traducción y técnicas de edición genética CRISPR-Cas9.",
            iconName = "biotech",
            pages = listOf(
                AcademicPage(
                    pageNumber = 1,
                    chapterTitle = "Capítulo 1: Dogma Central y Estructura del ADN",
                    contentText = """
                    El ácido desoxirribonucleico (ADN) almacena la información genética de todos los organismos vivos. Estudiado por Watson, Crick y Rosalind Franklin, el ADN forma una doble hélice antiparalela con un esqueleto exterior de fosfato-desoxirribosa y peldaños interiores de pares de bases nitrogenadas unidas por puentes de hidrógeno.

                    La adenina (A) se aparea exclusivamente con la timina (T) mediante dos enlaces de hidrógeno, mientras que la guanina (G) se aparea con la citosina (C) mediante tres enlaces de hidrógeno (Reglas de Chargaff).

                    El Dogma Central de la Biología Molecular establece el flujo unidireccional de información: ADN (Replicación) ➔ ARN (Transcripción) ➔ Proteínas (Traducción). La síntesis de proteínas es orquestada en los ribosomas mediante la lectura de codones de 3 nucleótidos en el ARNm.
                    """.trimIndent(),
                    defaultHighlights = listOf(
                        "doble hélice antiparalela",
                        "A-T (2 puentes de H) y G-C (3 puentes de H)",
                        "ADN ➔ ARN ➔ Proteínas"
                    ),
                    keyTerms = listOf("Bases Nitrogenadas", "Doble Hélice", "Codón", "ARN Mensajero"),
                    reviewQuestions = listOf(
                        "¿Por qué los pares G-C confieren mayor estabilidad térmica a la doble hélice?",
                        "Describe el papel de la ARN polimerasa durante la transcripción."
                    )
                ),
                AcademicPage(
                    pageNumber = 2,
                    chapterTitle = "Capítulo 2: Mitosis, Meiosis y Recombinación",
                    contentText = """
                    El ciclo celular eucariota comprende la interfase (fases G1, S y G2) y la división celular (fase M). La mitosis produce dos células hijas genéticamente idénticas y consta de profase, metafase, anafase y telofase.

                    Por el contrario, la meiosis es un proceso de división reduccional especializado en células germinales que genera cuatro gametos haploides (n). Durante la profase I meiótica, ocurre el sobrecruzamiento (crossing-over) entre cromátidas homólogas no hermanas en los quiasmas, lo cual incrementa exponencialmente la diversidad genética de las especies mediante recombinación homóloga.
                    """.trimIndent(),
                    defaultHighlights = listOf(
                        "mitosis produce dos células hijas genéticamente idénticas",
                        "meiosis genera cuatro gametos haploides",
                        "sobrecruzamiento (crossing-over) en profase I"
                    ),
                    keyTerms = listOf("Mitosis", "Meiosis", "Quiasmas", "Recombinación Homóloga"),
                    reviewQuestions = listOf(
                        "Compara las diferencias clave entre anafase mitótica y anafase I meiótica.",
                        "¿Cuál es la importancia evolutiva del crossing-over?"
                    )
                ),
                AcademicPage(
                    pageNumber = 3,
                    chapterTitle = "Capítulo 3: Biotecnología y Edición Genética CRISPR-Cas9",
                    contentText = """
                    El sistema CRISPR-Cas9, derivado de un mecanismo de defensa inmunológica adaptativa bacteriana contra bacteriófagos, ha revolucionado la ingeniería genética moderna.

                    Una molécula de ARN guía (sgRNA) dirige a la endonucleasa Cas9 hacia una secuencia diana complementaria de 20 nucleótidos en el genoma, adyacente a un motivo PAM (Proto-spacer Adjacent Motif). Cas9 genera un corte de doble cadena que la célula repara mediante unión de extremos no homólogos (NHEJ, causando deleciones/inactivación génica) o recombinación homóloga (HDR, permitiendo la inserción precisa de genes correctores).
                    """.trimIndent(),
                    defaultHighlights = listOf(
                        "ARN guía (sgRNA) dirige a la endonucleasa Cas9",
                        "motivo PAM (Proto-spacer Adjacent Motif)",
                        "corte de doble cadena reparado por HDR o NHEJ"
                    ),
                    keyTerms = listOf("CRISPR-Cas9", "ARN Guía", "Motivo PAM", "Terapia Génica"),
                    reviewQuestions = listOf(
                        "¿Cómo reconoce Cas9 la secuencia específica en el genoma?",
                        "¿Qué diferencias existen entre la reparación por NHEJ y HDR?"
                    )
                )
            )
        ),
        AcademicDocument(
            id = "doc_algoritmos",
            title = "Estructuras de Datos y Algoritmos Avanzados",
            subject = "Ciencias de la Computación",
            author = "Ing. Marcos Silveira",
            totalPages = 3,
            summary = "Notación Asintótica Big-O, grafos, caminos mínimos (Dijkstra, Bellman-Ford) y programación dinámica.",
            iconName = "terminal",
            pages = listOf(
                AcademicPage(
                    pageNumber = 1,
                    chapterTitle = "Capítulo 1: Análisis Asintótico y Complejidad Big-O",
                    contentText = """
                    El análisis de algoritmos evalúa los recursos temporales y espaciales requeridos en función del tamaño de entrada N. La notación Big-O (O) describe la cota superior asintótica en el peor de los casos.

                    • O(1) Tiempo constante: Acceso directo por índice en arreglos o tablas Hash.
                    • O(log N) Logarítmico: Búsqueda binaria en listas ordenadas y árboles binarios balanceados (AVL, Red-Black).
                    • O(N) Lineal: Recorrido simple de una lista enlazada o búsqueda secuencial.
                    • O(N log N) Cuasilineal: Algoritmos de ordenamiento óptimos basados en comparaciones como MergeSort y HeapSort.
                    • O(N²) Cuadrático: Algoritmos elementales como BubbleSort e InsertionSort.
                    """.trimIndent(),
                    defaultHighlights = listOf(
                        "Big-O describe la cota superior asintótica",
                        "O(log N) Búsqueda binaria",
                        "O(N log N) MergeSort y HeapSort"
                    ),
                    keyTerms = listOf("Notación Big-O", "Complejidad Temporal", "Cota Superior", "Árboles Balanceados"),
                    reviewQuestions = listOf(
                        "Demuestra por qué ningún algoritmo de ordenamiento por comparación puede ser menor a O(N log N).",
                        "Explica la diferencia entre Big-O, Big-Omega y Big-Theta."
                    )
                ),
                AcademicPage(
                    pageNumber = 2,
                    chapterTitle = "Capítulo 2: Algoritmos de Grafos y Caminos Mínimos",
                    contentText = """
                    Un grafo G = (V, E) consta de un conjunto de vértices V y aristas E. Los recorridos fundamentales son BFS (Breadth-First Search, usa cola FIFO y encuentra caminos más cortos en grafos no ponderados) y DFS (Depth-First Search, usa pila LIFO/recursión y detecta ciclos y componentes fuertemente conexas).

                    Para grafos ponderados con pesos no negativos, el algoritmo de Dijkstra encuentra las rutas más cortas desde un nodo origen utilizando una cola de prioridad (Min-Heap) con complejidad O((|V| + |E|) log |V|). En presencia de aristas con pesos negativos, se utiliza Bellman-Ford (O(|V| · |E|)), capaz de detectar ciclos negativos de coste infinito.
                    """.trimIndent(),
                    defaultHighlights = listOf(
                        "BFS encuentra caminos más cortos en grafos no ponderados",
                        "Dijkstra utiliza cola de prioridad Min-Heap",
                        "Bellman-Ford detecta ciclos negativos"
                    ),
                    keyTerms = listOf("Algoritmo de Dijkstra", "BFS y DFS", "Cola de Prioridad", "Bellman-Ford"),
                    reviewQuestions = listOf(
                        "¿Por qué Dijkstra falla cuando existen pesos negativos?",
                        "¿Cómo se modela una red de transporte aéreo usando grafos?"
                    )
                ),
                AcademicPage(
                    pageNumber = 3,
                    chapterTitle = "Capítulo 3: Programación Dinámica y Memoización",
                    contentText = """
                    La programación dinámica es un paradigma de optimización aplicable a problemas con dos propiedades clave: subestructura óptima y subproblemas superpuestos.

                    Existen dos enfoques primarios:
                    1. Top-Down con Memoización: Se parte del problema general resolviéndolo de manera recursiva y cacheando los resultados ya calculados en un mapa o arreglo.
                    2. Bottom-Up con Tabulación: Se resuelven iterativamente los casos base más pequeños construyendo una matriz o tabla hasta alcanzar la solución final.

                    Ejemplos clásicos incluyen el Problema de la Mochila (Knapsack 0/1), la subsecuencia común más larga (LCS) y el cálculo de la distancia de edición (Levenshtein).
                    """.trimIndent(),
                    defaultHighlights = listOf(
                        "subestructura óptima y subproblemas superpuestos",
                        "Top-Down con Memoización vs Bottom-Up con Tabulación",
                        "Problema de la Mochila y LCS"
                    ),
                    keyTerms = listOf("Programación Dinámica", "Memoización", "Tabulación", "Problema de la Mochila"),
                    reviewQuestions = listOf(
                        "Explica la ecuación de recurrencia para el problema de la mochila 0/1.",
                        "¿Cuál es el beneficio en consumo de memoria de Tabulación frente a Recursión?"
                    )
                )
            )
        )
    )
}
