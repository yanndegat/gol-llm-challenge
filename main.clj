(ns main
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io])
  (:gen-class))

(def source-file "main.clj")
(def state-file "program-state.json")
(def html-file "conway.html")

(def api-endpoint (System/getenv "API_URL"))
(def api-key (System/getenv "API_KEY"))
(def model (System/getenv "API_MODEL"))

(defn create-html []
  (spit html-file
"<!DOCTYPE html>
<html>
<head>
    <title>Conway's Game of Life</title>
    <style>
        body { font-family: Arial, sans-serif; text-align: center; }
        canvas { background: #000; margin: 20px auto; display: block; }
        button { padding: 10px 20px; margin: 5px; }
        .controls { margin: 20px; }
    </style>
</head>
<body>
    <h1>Conway's Game of Life</h1>
    <div class=\"controls\">
        <button id=\"start\">Start</button>
        <button id=\"stop\">Stop</button>
        <button id=\"reset\">Reset</button>
        <button id=\"random\">Random</button>
    </div>
    <canvas id=\"game\" width=\"500\" height=\"500\"></canvas>
    <script>
        const canvas = document.getElementById('game');
        const ctx = canvas.getContext('2d');
        const cellSize = 10;
        const cols = Math.floor(canvas.width / cellSize);
        const rows = Math.floor(canvas.height / cellSize);
        
        let grid = createEmptyGrid();
        let running = false;
        let animationId = null;

        function createEmptyGrid() {
            return Array(rows).fill().map(() => Array(cols).fill(0));
        }

        function createRandomGrid() {
            return Array(rows).fill().map(() => 
                Array(cols).fill().map(() => Math.random() > 0.7 ? 1 : 0)
            );
        }

        function drawGrid() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            for (let i = 0; i < rows; i++) {
                for (let j = 0; j < cols; j++) {
                    if (grid[i][j]) {
                        ctx.fillStyle = '#00ff00';
                        ctx.fillRect(j * cellSize, i * cellSize, cellSize - 1, cellSize - 1);
                    }
                }
            }
        }

        function countNeighbors(grid, x, y) {
            let sum = 0;
            for (let i = -1; i < 2; i++) {
                for (let j = -1; j < 2; j++) {
                    const row = (x + i + rows) % rows;
                    const col = (y + j + cols) % cols;
                    sum += grid[row][col];
                }
            }
            sum -= grid[x][y];
            return sum;
        }

        function updateGrid() {
            const nextGrid = createEmptyGrid();
            for (let i = 0; i < rows; i++) {
                for (let j = 0; j < cols; j++) {
                    const neighbors = countNeighbors(grid, i, j);
                    if (grid[i][j] === 1 && (neighbors < 2 || neighbors > 3)) {
                        nextGrid[i][j] = 0;
                    } else if (grid[i][j] === 0 && neighbors === 3) {
                        nextGrid[i][j] = 1;
                    } else {
                        nextGrid[i][j] = grid[i][j];
                    }
                }
            }
            grid = nextGrid;
        }

        function gameLoop() {
            updateGrid();
            drawGrid();
            if (running) {
                animationId = requestAnimationFrame(gameLoop);
            }
        }

        document.getElementById('start').addEventListener('click', () => {
            if (!running) {
                running = true;
                gameLoop();
            }
        });

        document.getElementById('stop').addEventListener('click', () => {
            running = false;
            cancelAnimationFrame(animationId);
        });

        document.getElementById('reset').addEventListener('click', () => {
            grid = createEmptyGrid();
            drawGrid();
        });

        document.getElementById('random').addEventListener('click', () => {
            grid = createRandomGrid();
            drawGrid();
        });

        canvas.addEventListener('click', (e) => {
            const rect = canvas.getBoundingClientRect();
            const x = Math.floor((e.clientX - rect.left) / cellSize);
            const y = Math.floor((e.clientY - rect.top) / cellSize);
            grid[y][x] = grid[y][x] ? 0 : 1;
            drawGrid();
        });

        grid = createRandomGrid();
        drawGrid();
    </script>
</body>
</html>"))

(defn open-browser []
  (try
    (let [os (System/getProperty "os.name").toLowerCase]
      (cond
        (.contains os "win") (shell/sh "cmd" "/c" "start" html-file)
        (.contains os "mac") (shell/sh "open" html-file)
        :else (shell/sh "xdg-open" html-file)))
    (catch Exception e
      (println "⚠️  Couldn't open browser:" (.getMessage e)))))

(defn read-source []
  (slurp source-file))

(defn write-source [code]
  (spit source-file code))

(defn read-state []
  (try
    (json/parse-string (slurp state-file) true)
    (catch Exception _
      {:iteration 0 :progress-log [] :current-phase "initialization"})))

(defn write-state [state]
  (spit state-file (json/generate-string state {:pretty true})))

(defn update-state [state success? change-description]
  (let [new-iteration (inc (:iteration state))
        log-entry (str "Iter " new-iteration ": "
                       (if success? "✅ " "❌ ")
                       change-description)]
    (-> state
        (assoc :iteration new-iteration)
        (update :progress-log conj log-entry))))

(defn -main [& _args]
  (println "🚀 Launching Conway's Game of Life")
  (create-html)
  (println "🌐 Opening in browser...")
  (open-browser))