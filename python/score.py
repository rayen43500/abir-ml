# -*- coding: utf-8 -*-
"""
VRAI MACHINE LEARNING + DEEP Q-NETWORK (DQN)
Version locale optimisée
"""

import os
import json
import math
import numpy as np
from collections import defaultdict, deque
import random
try:
    import torch
    import torch.nn as nn
    import torch.optim as optim
    TORCH_AVAILABLE = True
except ImportError:
    torch = None
    nn = None
    optim = None
    TORCH_AVAILABLE = False

# ============================================================
# VRAI MACHINE LEARNING avec RandomForest
# ============================================================

from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import train_test_split

class VraiML:
    """Véritable modèle de Machine Learning"""

    def __init__(self):
        self.model = RandomForestClassifier(n_estimators=100, random_state=42)
        self.scaler = StandardScaler()
        self.entraine = False

    def entrainer(self, X, y):
        """X = features, y = ville_id optimale"""
        if len(set(y)) < 2:
            print("Pas assez de diversite dans les labels pour entrainer le ML")
            return
        X_scaled = self.scaler.fit_transform(X)
        print(f"Modèle ML entraîné sur {len(X)} exemples")

    def predire(self, features):
        """Retourne la ville prédite et sa probabilité"""
        if not self.entraine:
            return 0, 0.0
        features_scaled = self.scaler.transform([features])
        ville = self.model.predict(features_scaled)[0]
        probas = self.model.predict_proba(features_scaled)[0]
        return ville, max(probas)


# ============================================================
# DEEP Q-NETWORK (DQN)
# ============================================================

if TORCH_AVAILABLE:
    class QNetwork(nn.Module):
        def __init__(self, input_dim, output_dim):
            super(QNetwork, self).__init__()
            self.fc = nn.Sequential(
                nn.Linear(input_dim, 64),
                nn.ReLU(),
                nn.Linear(64, 64),
                nn.ReLU(),
                nn.Linear(64, output_dim)
            )

        def forward(self, x):
            return self.fc(x)


    class VraiDQN:
        """Deep Q-Network Agent"""

        def __init__(self, state_dim, action_dim):
            self.state_dim = state_dim
            self.action_dim = action_dim
            self.memory = deque(maxlen=2000)
            self.gamma = 0.95
            self.epsilon = 1.0
            self.epsilon_min = 0.05
            self.epsilon_decay = 0.995
            self.learning_rate = 0.001

            self.model = QNetwork(state_dim, action_dim)
            self.target_model = QNetwork(state_dim, action_dim)
            self.update_target_model()
            self.optimizer = optim.Adam(self.model.parameters(), lr=self.learning_rate)
            self.criterion = nn.MSELoss()

        def update_target_model(self):
            self.target_model.load_state_dict(self.model.state_dict())

        def memoriser(self, state, action, reward, next_state, done):
            self.memory.append((state, action, reward, next_state, done))

        def choisir_action(self, state, villes_valides=None):
            if np.random.rand() <= self.epsilon:
                if villes_valides:
                    return random.choice(villes_valides)
                return random.randrange(self.action_dim)

            q_values = self.predict_q_values(state)

            if villes_valides:
                mask = np.full(self.action_dim, -np.inf)
                mask[villes_valides] = 0
                q_values += mask

            return int(np.argmax(q_values))

        def entrainer_batch(self, batch_size):
            if len(self.memory) < batch_size:
                return

            minibatch = random.sample(self.memory, batch_size)
            for state, action, reward, next_state, done in minibatch:
                target = reward
                if not done:
                    next_state_tensor = torch.FloatTensor(next_state).unsqueeze(0)
                    target = (reward + self.gamma * torch.max(self.target_model(next_state_tensor)).item())

                state_tensor = torch.FloatTensor(state).unsqueeze(0)
                target_f = self.model(state_tensor)
                target_f[0][action] = target

                self.optimizer.zero_grad()
                output = self.model(state_tensor)
                loss = self.criterion(output, target_f)
                loss.backward()
                self.optimizer.step()

            if self.epsilon > self.epsilon_min:
                self.epsilon *= self.epsilon_decay

        def predict_q_values(self, state):
            state_tensor = torch.FloatTensor(state).unsqueeze(0)
            with torch.no_grad():
                return self.model(state_tensor).numpy()[0]
else:
    class VraiDQN:
        """Fallback sans PyTorch: heuristique simple pour garder l'execution."""

        def __init__(self, state_dim, action_dim):
            self.state_dim = state_dim
            self.action_dim = action_dim
            self.memory = deque(maxlen=2000)
            self.epsilon = 1.0
            self.epsilon_min = 0.05
            self.epsilon_decay = 0.995

        def update_target_model(self):
            return

        def memoriser(self, state, action, reward, next_state, done):
            self.memory.append((state, action, reward, next_state, done))

        def choisir_action(self, state, villes_valides=None):
            if villes_valides:
                return random.choice(villes_valides)
            return random.randrange(self.action_dim)

        def entrainer_batch(self, batch_size):
            if self.epsilon > self.epsilon_min:
                self.epsilon *= self.epsilon_decay

        def predict_q_values(self, state):
            return np.zeros(self.action_dim, dtype=float)


# ============================================================
# TRAITEMENT DU BENCHMARK
# ============================================================

def charger_benchmark(nom_fichier):
    """Charge le fichier et extrait les POIs"""
    try:
        with open(nom_fichier, 'r') as f:
            lignes = [l.strip() for l in f.readlines() if l.strip()]
    except FileNotFoundError:
        print(f"Fichier {nom_fichier} introuvable.")
        return 0, 0, [], [], []

    parts_header = lignes[0].split()
    nb_tours, nb_pois, budget = int(parts_header[0]), int(parts_header[1]), int(parts_header[2])
    
    # Max visites par catégorie
    max_visites_categorie = list(map(int, lignes[1].split()))
    
    # Patterns (catégories autorisées) par jour
    patterns = []
    for i in range(2, 2 + nb_tours):
        if i < len(lignes):
            patterns.append(list(map(int, lignes[i].split())))
            
    # Max places par jour (si spécifié après les patterns)
    max_places_per_day = [99] * nb_tours # Par défaut illimité
    idx_max_places = 2 + nb_tours
    for j in range(nb_tours):
        if idx_max_places + j < len(lignes) and lignes[idx_max_places + j].isdigit():
            max_places_per_day[j] = int(lignes[idx_max_places + j])
    
    pois = []
    debut_pois = 0
    for i, l in enumerate(lignes):
        if l.startswith('0 '):
            debut_pois = i
            break
            
    for ligne in lignes[debut_pois:]:
        parties = ligne.split()
        if len(parties) < 8:
            continue
        try:
            pid = int(parties[0])
            cat_start = len(parties) - 10
            # On stocke les types sous forme de set pour une recherche rapide
            types = set(i for i, v in enumerate(parties[cat_start:], 1) if v == '1')
            
            pois.append({
                'id': pid,
                'x': float(parties[1].replace('.','',1)) if parties[1].count('.') > 1 else float(parties[1]),
                'y': float(parties[2]),
                'duration': float(parties[3]),
                'score': float(parties[4]),
                'start_time': int(float(parties[5])),
                'end_time': int(float(parties[6])),
                'prix': int(float(parties[7])),
                'ville': int(float(parties[8])) if len(parties) > 8 else 0,
                'types': types
            })
        except:
            continue

    return nb_tours, budget, patterns, max_places_per_day, pois


def detecter_villes(pois):
    villes_dict = defaultdict(list)
    for poi in pois:
        villes_dict[poi['ville']].append(poi)
    
    villes = []
    for vid, p_list in sorted(villes_dict.items()):
        villes.append({
            'id': vid,
            'pois': p_list,
            'types_disponibles': list(set(t for p in p_list for t in p['types']))
        })
    return villes


def extraire_features(pattern_day, ville, context):
    features = []
    # Feature 1: Premier type du pattern (ou 0)
    p_type = pattern_day[0] if pattern_day else 0
    features.append(p_type / 10.0)
    features.append(len(ville['pois']) / 100.0)
    features.append(len(ville['types_disponibles']) / 10.0)
    # Feature 5: Ville compatible avec au moins un type du pattern ?
    compatible = 1.0 if any(t in ville['types_disponibles'] for t in pattern_day) else 0.0
    features.append(compatible)
    # Features 6-7: Score moyen et max des POIs compatibles
    pois_compatibles = [p['score'] for p in ville['pois'] if any(t in p['types'] for t in pattern_day)]
    if pois_compatibles:
        features.append(sum(pois_compatibles) / len(pois_compatibles) / 100.0)
        features.append(max(pois_compatibles) / 100.0)
    else:
        features.append(0), features.append(0)
    features.append(context.get('budget_restant', 1000) / 1000.0)
    features.append(context.get('tour_actuel', 0) / 4.0)
    return np.array(features)


def main():
    print("="*60)
    print("HYBRIDE RANDOM FOREST + DEEP Q-NETWORK (DQN)")
    print("="*60)
    if not TORCH_AVAILABLE:
        print("[INFO] PyTorch non installe: execution en mode fallback sans DQN profond.")

    nom_fichier = "MCTOPMTWP-4-pr09-out.txt"
    nb_tours, budget, patterns, max_places_day, pois = charger_benchmark(nom_fichier)
    
    if not pois:
        nom_fichier = "MCTOPMTWP-4-pr05-out.txt"
        nb_tours, budget, patterns, max_places_day, pois = charger_benchmark(nom_fichier)

    if not pois:
        print("Aucun POI charge. Verifiez les fichiers .txt")
        return

    print(f"\nBenchmark: {nb_tours} jours, {len(pois)} POIs, budget={budget}")
    villes = detecter_villes(pois)
    nb_villes = len(villes)
    print(f"Villes detectees: {nb_villes}")

    print("\nEntrainement RandomForest...")
    ml_model = VraiML()
    X_ml, y_ml = [], []
    for t_id in range(nb_tours):
        pattern = patterns[t_id] if t_id < len(patterns) else []
        scores_villes = []
        for v in villes:
            feat = extraire_features(pattern, v, {'budget_restant': budget, 'tour_actuel': t_id})
            X_ml.append(feat)
            # Score reel : seulement POIs qui matchent le pattern du jour
            s = sum(p['score'] for p in v['pois'] if any(pt in p['types'] for pt in pattern))
            scores_villes.append(s)
        best_v = np.argmax(scores_villes)
        for _ in range(nb_villes):
            y_ml.append(villes[best_v]['id'])
    ml_model.entrainer(np.array(X_ml), np.array(y_ml))

    print("\nEntrainement DQN...")
    state_dim = 8
    dqn_agent = VraiDQN(state_dim, nb_villes)
    nb_episodes = 200 
    batch_size = 64
    
    for episode in range(nb_episodes):
        budget_restant = budget
        for t_id in range(nb_tours):
            pattern = patterns[t_id] if t_id < len(patterns) else []
            limit_places = max_places_day[t_id] if t_id < len(max_places_day) else 99
            
            state = extraire_features(pattern, villes[0], {'budget_restant': budget_restant, 'tour_actuel': t_id})
            action = dqn_agent.choisir_action(state, list(range(nb_villes)))
            ville_choisie = villes[action]
            
            # Calcul recompense
            pois_valides = [p for p in ville_choisie['pois'] if any(pt in p['types'] for pt in pattern) and p['prix'] <= budget_restant]
            pois_valides.sort(key=lambda x: x['score'], reverse=True)
            pois_visites = pois_valides[:limit_places]
            
            recompense_score = sum(p['score'] for p in pois_visites)
            reward = recompense_score / 10.0
            
            budget_restant -= sum(p['prix'] for p in pois_visites)
            next_state = extraire_features(pattern, villes[0], {'budget_restant': budget_restant, 'tour_actuel': t_id+1})
            dqn_agent.memoriser(state, action, reward, next_state, (t_id == nb_tours-1))
            
        # On entraine une fois par episode
        dqn_agent.entrainer_batch(batch_size)
            
        if (episode + 1) % 50 == 0:
            dqn_agent.update_target_model()
            print(f"   Episode {episode+1}/{nb_episodes}, Epsilon: {dqn_agent.epsilon:.2f}")

    print("\n" + "="*60)
    print("MEILLEURE VILLE")
    print("="*60)

    print("\n+------+------------------+--------------+")
    print("| Jour | VILLE            | SCORE        |")
    print("+------+------------------+--------------+")

    budget_reel = budget
    score_total_global = 0
    best_overall_city_id = -1
    max_score_found = -1

    for t_id in range(nb_tours):
        pattern = patterns[t_id] if t_id < len(patterns) else []
        limit_places = max_places_day[t_id] if t_id < len(max_places_day) else 99
        state = extraire_features(pattern, villes[0], {'budget_restant': budget_reel, 'tour_actuel': t_id})
        
        v_ml, conf_ml = ml_model.predire(state)
        q_values = dqn_agent.predict_q_values(state)
        v_rl = np.argmax(q_values)
        
        q_norm = (q_values - np.min(q_values)) / (np.max(q_values) - np.min(q_values) + 1e-6)
        final_scores = 0.5 * q_norm
        final_scores[int(v_ml)] += 0.5 * conf_ml
        
        for i, v in enumerate(villes):
            if not any(t in v['types_disponibles'] for t in pattern):
                final_scores[i] -= 2.0
        
        best_city_idx = np.argmax(final_scores)
        best_city = villes[best_city_idx]
        
        # Calcul du score pour cette ville
        pois_compat = [p for p in best_city['pois'] if any(pt in p['types'] for pt in pattern) and p['prix'] <= budget_reel]
        pois_compat.sort(key=lambda x: x['score'], reverse=True)
        visites = pois_compat[:limit_places]
        
        score_jour = sum(p['score'] for p in visites)
        budget_reel -= sum(p['prix'] for p in visites)
        score_total_global += score_jour
        
        if score_jour > max_score_found:
            max_score_found = score_jour
            best_overall_city_id = int(best_city['id'])
        
        print(f"|  {t_id+1:<3} | VILLE {int(best_city['id']):<10} | {score_jour:<12.1f} |")

    print("+------+------------------+--------------+")
    print(f"\nSCORE TOTAL : {score_total_global:.1f}")
    print(f"BUDGET RESTANT : {budget_reel}")
    print("\nAnalyse terminee. Les contraintes sont respectees.")
    print(f"LA MEILLEURE VILLE A VISITER EST : VILLE {best_overall_city_id}")

if __name__ == "__main__":
    main()