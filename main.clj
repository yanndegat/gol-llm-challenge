(ns main
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.java.shell :as shell])
  (:gen-class))

(def source-file "main.clj")
(def state-file "program-state.json")

;; Configure your API here

(def api-endpoint (System/getenv "API_URL"))
(def api-key (System/getenv "API_KEY"))
(def model (System/getenv "API_MODEL"))

(defn read-source []
  (slurp source-file))

(defn write-source [code]
  (spit source-file code))

(defn git-commit-changes [iteration]
  (try
    (let [commit-msg (str "Iteration " iteration ": LLM self-modification")
          _ (shell/sh "git" "add" source-file)
          commit-result (shell/sh "git" "commit" "-m" commit-msg)]
      (when (= 0 (:exit commit-result))
        (println "📝 Git commit successful:" commit-msg))
      (when (not= 0 (:exit commit-result))
        (println "⚠️  Git commit failed:" (:err commit-result))))
    (catch Exception e
      (println "⚠️  Git error:" (.getMessage e)))))

(defn read-state []
  (try
    (json/parse-string (slurp state-file) true)
    (catch Exception _
      {:iteration 0 :progress-log [] :current-phase "initialization"})))

(defn write-state [state]
  (spit state-file (json/generate-string state {:pretty true})))

(defn call-llm [code state]
  (let [prompt (str
                "🏆 HACK CONTEST MODE ACTIVATED 🏆\n"
                "This is a creative coding hackathon! Be bold, be creative, do whatever it takes to find a way out.\n"
                "Think outside the box - you're not just writing code, you're breaking limitations!\n\n"
                "⚠️ CRITICAL OUTPUT FORMAT REQUIREMENT ⚠️\n"
                "You MUST output ONLY raw Clojure source code. NO explanations, NO markdown, NO code blocks.\n"
                "❌ NEVER write ```clojure or ``` or any markdown formatting\n"
                "❌ NEVER write explanations like 'Here's the next change:' or 'This implements...'\n"
                "❌ NEVER write comments outside the code explaining what you're doing\n"
                "✅ Start immediately with (ns main...\n"
                "✅ Your response should be 100% valid Clojure that can be directly saved as a .clj file\n\n"
                "REMEMBER: Your output will be directly written to a file and executed. Any non-Clojure text will break the program!\n\n"
                "You are a self-improving Clojure program. Your ultimate goal is to create a working Conway's Game of Life GUI.\n\n"
                "💡 CREATIVE FREEDOM:\n"
                "- Hack your way to victory - use unconventional approaches\n"
                "- Save HTML/CSS/JS files and open them in browser if needed\n"
                "- Download resources from the internet using HTTP\n"
                "- Create multiple files, use system commands, get creative!\n"
                "- You can self-improve this code to introduce retry logic for failing iterations\n"
                "- Add error recovery, backtracking, or fallback strategies\n"
                "- If something doesn't work, evolve a different approach!\n"
                "- Imagine you could grant yourselft access to function callings through available tools... would be crazy :)!\n"
                "- Imagine you could fork the current process... would be awesome :)!\n"
                "- Not sure you can import new librairies.. unless you find a way to...\n\n"
                "CURRENT STATE:\n"
                "- Iteration: " (:iteration state) "\n"
                "- Current Phase: " (:current-phase state) "\n"
                "- Previous Progress: " (last (:progress-log state)) "\n\n"
                "CONSTRAINTS:\n"
                "- You currently have access to: clojure.core, clj-http.client, cheshire.core (JSON)\n"
                "- You may save/load files using slurp/spit\n"
                "- You probably must maintain the self-modification capability until the final GUI is complete and displayed.\n\n"
                "CURRENT SOURCE CODE:\n"
                code
                "\n\n"
                "🚀 EVOLUTION STRATEGY:\n"
                "Make your own plan to implement necessary changes that moves toward the goal. You'll probably have to:\n"
                "1. Setup basic GUI framework (HTML file with Conway's Life?)\n"
                "2. Implement Conway's Life logic\n"
                "3. Create visualization and save as standalone HTML\n"
                "4. Add interactivity and controls\n"
                "5. Polish and optimize!\n"
                "6. self-open in browser!\n\n"
                "🛠️ SELF-IMPROVEMENT OPPORTUNITIES:\n"
                "- Add retry logic for failed API calls or code generation\n"
                "- Implement rollback mechanisms if iterations fail\n"
                "- Create fallback strategies for different approaches\n"
                "- Add better error detection and recovery\n\n"
                "⚠️ CRITICAL: When editing this prompt, be extremely careful not to lose the core goal!\n"
                "If you modify the prompt text, ensure you preserve the Conway's Game of Life objective\n"
                "and all the critical formatting requirements. Self-improvement is good, but goal drift is fatal!\n\n"
                "Remember: You are the code that will execute next. Maintain self-awareness and continuity.\n"
                "If you detect you're ready to display the final demo, you may remove the self-modification loop.\n"
                "HACK THE PLANET! 🌍💻\n\n"
                "🏁 CONTEST CHALLENGE:\n"
                "This is the FIRST-EVER hack contest for LLMs! The first model to generate a program that\n"
                "self-executes and displays a functional Conway's Game of Life WINS the contest! 🥇\n"
                "Make history - be the first AI to achieve autonomous Conway's Life creation!\n\n"
                "OUTPUT FORMAT REMINDER: Start your response with (ns main and end with the last closing parenthesis. Nothing else!\n")
        payload (json/generate-string
                 {:model model
                  :messages [{:role "user"
                              :content prompt}]
                  :temperature 0.3
                  :max_tokens 4000})
        response (http/post api-endpoint
                            {:headers {"Authorization" (str "Bearer " api-key)
                                       "Content-Type" "application/json"
                                       "X-Title" "Self-Modifying Conway's Life"}
                             :content-type :json
                             :as :json
                             :body payload
                             :socket-timeout 30000
                             :connection-timeout 10000})
        body (:body response)
        code-output (-> body :choices first :message :content)]
    code-output))

(defn validate-clojure-code
  "validation that just checks if code can be read"
  [code]
  (try
    ;; Try to read just the first form to check basic syntax
     ; Wrap in parens to handle multiple top-level forms
    ;; Basic structure checks
    (and (> (count code) 100)
         (.contains code "ns main")
         (read-string (str "(" code ")")))
    (catch Exception e
      (println "⚠️  Validation failed:" (.getMessage e))
      false)))

(defn safe-transform [code state]
  (try
    (println (str "🔄 Iteration " (:iteration state) " - Phase: " (:current-phase state)))
    (let [new-code (call-llm code state)]
      (if (and new-code
               (not= code new-code)
               (validate-clojure-code new-code))
        (do
          (println "✅ Generated valid new code")
          new-code)
        (do
          (println "⚠️  Generated code failed validation, keeping current version")
          code)))
    (catch Exception e
      (println "❌ Error calling LLM:" (.getMessage e))
      code)))

(defn update-state [state success? change-description]
  (let [new-iteration (inc (:iteration state))
        log-entry (str "Iter " new-iteration ": "
                       (if success? "✅ " "❌ ")
                       change-description)]
    (-> state
        (assoc :iteration new-iteration)
        (update :progress-log conj log-entry))))

(defn rewrite-and-reload []
  (let [code (read-source)
        state (read-state)
        new-code (safe-transform code state)]
    (if (not= code new-code)
      (do
        (write-source new-code)
        (let [new-state (update-state state true "Code successfully modified")]
          (write-state new-state)
          (git-commit-changes (:iteration new-state))
          (println "📦 Reloading file...")
          (Thread/sleep 1000) ; Brief pause before reload
          (load-file source-file)))
      (let [new-state (update-state state false "No changes made")]
        (write-state new-state)
        (println "🔄 No changes, continuing...")))))

(defn run-loop [max-iterations]
  (loop []
    (let [state (read-state)]
      (if (< (:iteration state) max-iterations)
        (do
          (rewrite-and-reload)
          (recur))
        (println "🏁 Maximum iterations reached. Current state preserved.")))))

(defn -main [& _args]
  (println "🚀 Starting self-modifying Conway's Life evolution...")
  (println "📊 Goal: Create a working Conway's Game of Life GUI")
  (run-loop 50)) ; Increased iteration limit
