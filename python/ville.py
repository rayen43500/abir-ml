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
import torch
import torch.nn as nn
import torch.optim as optim

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
        """X = features, y = 1 (bonne ville) ou 0 (autre)"""
        if len(set(y)) < 2:
            print("Pas assez de diversite dans les labels pour entrainer le ML")
            return
        X_scaled = self.scaler.fit_transform(X)
        self.model.fit(X_scaled, y)
        self.entraine = True
        print(f"Modèle ML entraîné sur {len(X)} exemples")

    def predire(self, features):
        """Retourne la probabilité que la ville soit un bon choix"""
        if not self.entraine:
            return 0.0
        features_scaled = self.scaler.transform([features])
        # Retourne la probabilité de la classe 1 (bonne ville)
        probas = self.model.predict_proba(features_scaled)[0]
        if len(probas) > 1:
            return probas[1]
        return 1.0 if self.model.predict(features_scaled)[0] == 1 else 0.0


# ============================================================
# DEEP Q-NETWORK (DQN)
# ============================================================

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
        
        state_tensor = torch.FloatTensor(state).unsqueeze(0)
        with torch.no_grad():
            q_values = self.model(state_tensor).numpy()[0]
        
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


def extraire_features(pattern_day, poi, context):
    """Features au niveau du POI pour une meilleure précision"""
    features = []
    # Pattern du jour
    p_main = pattern_day[0] if pattern_day else 0
    features.append(p_main / 10.0)
    
    # Caractéristiques du POI
    features.append(poi['score'] / 150.0)
    features.append(poi['prix'] / 200.0)
    features.append(poi['duration'] / 30.0)
    
    # Compatibilité avec le pattern
    comp = 1.0 if any(t in poi['types'] for t in pattern_day) else 0.0
    features.append(comp)
    
    # Contexte global
    features.append(context.get('budget_restant', 1000) / 1500.0)
    features.append(context.get('tour_actuel', 0) / 5.0)
    
    # Feature 8 : ID normalisé pour la diversité
    features.append(poi['id'] / 100.0)
    
    return np.array(features)


def main():
    print("+" + "="*78 + "+")
    print("|" + " "*20 + "HYBRIDE RANDOM FOREST + DEEP Q-NETWORK" + " "*20 + "|")
    print("+" + "="*78 + "+")

    nom_fichier = "MCTOPMTWP-4-pr09-out.txt"
    nb_tours, budget, patterns, max_places_day, pois = charger_benchmark(nom_fichier)
    
    if not pois:
        nom_fichier = "MCTOPMTWP-4-pr05-out.txt"
        nb_tours, budget, patterns, max_places_day, pois = charger_benchmark(nom_fichier)

    if not pois:
        print("Aucun POI charge. Verifiez les fichiers .txt")
        return

    print(f"\nBenchmark: {nb_tours} jours, {len(pois)} POIs, budget={budget}")
    
    # On garde une trace des villes pour l'affichage, mais on travaille par POI
    villes_info = {p['id']: p['ville'] for p in pois}

    print("\nEntrainement RandomForest (Niveau POI)...")
    ml_model = VraiML()
    X_ml, y_ml = [], []
    for t_id in range(nb_tours):
        pattern = patterns[t_id] if t_id < len(patterns) else []
        # Pour chaque POI, est-il un bon candidat pour ce jour ?
        pois_sorted = sorted(pois, key=lambda p: p['score'] if any(t in p['types'] for t in pattern) else -1, reverse=True)
        top_n = [p['id'] for p in pois_sorted[:10]] # Top 10 POIs du jour
        
        for p in pois:
            feat = extraire_features(pattern, p, {'budget_restant': budget, 'tour_actuel': t_id})
            X_ml.append(feat)
            y_ml.append(1 if p['id'] in top_n else 0)
            
    ml_model.entrainer(np.array(X_ml), np.array(y_ml))

    print(f"\n[DQN] Debut de l'entrainement (Niveau POI)...")
    state_dim = 8
    nb_pois = len(pois)
    dqn_agent = VraiDQN(state_dim, nb_pois)
    nb_episodes = 200
    batch_size = 64
    episode_rewards = []
    for episode in range(nb_episodes):
        budget_restant = budget
        visited_ep = set()
        total_reward = 0
        for t_id in range(nb_tours):
            pattern = patterns[t_id] if t_id < len(patterns) else []
            limit_places = max_places_day[t_id] if t_id < len(max_places_day) else 99
            
            # Pour chaque slot de visite possible ce jour-là
            for _ in range(limit_places):
                # État basé sur le POI 0 par défaut pour la structure
                state = extraire_features(pattern, pois[0], {'budget_restant': budget_restant, 'tour_actuel': t_id})
                
                # Masquer les POIs déjà visités ou trop chers
                valides = [i for i, p in enumerate(pois) if i not in visited_ep and p['prix'] <= budget_restant]
                if not valides: break
                
                idx_poi = dqn_agent.choisir_action(state, valides)
                poi_choisi = pois[idx_poi]
                
                # Récompense
                bonus = 2.0 if any(t in poi_choisi['types'] for t in pattern) else 0.5
                reward = (poi_choisi['score'] / 50.0) * bonus
                
                total_reward += reward
                budget_restant -= poi_choisi['prix']
                visited_ep.add(idx_poi)
                
                next_state = extraire_features(pattern, pois[0], {'budget_restant': budget_restant, 'tour_actuel': t_id})
                dqn_agent.memoriser(state, idx_poi, reward, next_state, False)
            
        dqn_agent.entrainer_batch(batch_size)
        episode_rewards.append(total_reward)
            
        if (episode + 1) % 25 == 0:
            dqn_agent.update_target_model()
            avg_reward = np.mean(episode_rewards[-25:])
            print(f"   > Episode {episode+1:3}/{nb_episodes} | Avg Reward: {avg_reward:6.2f} | Epsilon: {dqn_agent.epsilon:.3f}")

    print("\n" + "="*80)
    print(" " * 30 + "ANALYSE ET RESULTATS")
    print("\n" + "+" + "-"*65 + "+")
    header = f"| {'Jour':^4} | {'POI ID':^8} | {'SCORE':^10} | {'COUT':^8} | {'CATEGORIE':^12} | {'ML %':^6} |"
    print(header)
    print("|" + "-"*6 + "|" + "-"*10 + "|" + "-"*12 + "|" + "-"*10 + "|" + "-"*14 + "|" + "-"*8 + "|")

    budget_reel = budget
    score_total_global = 0
    visited_final = set()
    
    for t_id in range(nb_tours):
        pattern = patterns[t_id] if t_id < len(patterns) else []
        limit_places = max_places_day[t_id] if t_id < len(max_places_day) else 99
        
        # Pour chaque tour, on sélectionne les meilleurs POIs globalement
        for _ in range(limit_places):
            state_rl = extraire_features(pattern, pois[0], {'budget_restant': budget_reel, 'tour_actuel': t_id})
            with torch.no_grad():
                q_values = dqn_agent.model(torch.FloatTensor(state_rl).unsqueeze(0)).numpy()[0]
            
            q_norm = (q_values - np.min(q_values)) / (np.max(q_values) - np.min(q_values) + 1e-6)
            
            # Calcul scores hybrides pour tous les POIs non visités
            final_scores = []
            for i, p in enumerate(pois):
                if i in visited_final or p['prix'] > budget_reel:
                    final_scores.append(-999)
                    continue
                
                feat_p = extraire_features(pattern, p, {'budget_restant': budget_reel, 'tour_actuel': t_id})
                conf_ml = ml_model.predire(feat_p)
                
                # Score = 0.5 ML + 0.5 RL + Bonus Catégorie
                score_h = 0.5 * conf_ml + 0.5 * q_norm[i]
                if any(t in p['types'] for t in pattern):
                    score_h += 1.0 # Fort bonus pour la catégorie demandée
                
                final_scores.append(score_h)
            
            best_poi_idx = np.argmax(final_scores)
            if final_scores[best_poi_idx] < -100: break # Plus de POIs possibles
            
            poi_selected = pois[best_poi_idx]
            visited_final.add(best_poi_idx)
            
            score_total_global += poi_selected['score']
            budget_reel -= poi_selected['prix']
            
            # Affichage ligne
            ml_p = ml_model.predire(extraire_features(pattern, poi_selected, {})) * 100
            cat_match = ", ".join(map(str, sorted(set(pattern) & poi_selected['types'])))
            if not cat_match: cat_match = "Autre"
            
            print(f"|  {t_id+1:2}  | {poi_selected['id']:^8} | {poi_selected['score']:10.1f} | {poi_selected['prix']:8} | {cat_match:^12} | {ml_p:5.1f}% |")

    print("+" + "-"*65 + "+")
    print(f"\n> SCORE TOTAL GLOBAL : {score_total_global:.1f}")
    print(f"> BUDGET FINAL RESTANT : {budget_reel}")
    print(f"> NOMBRE DE LIEUX VISITES : {len(visited_final)}")
    print("\n" + "="*80)
    print(f"  [BEST] OPTIMISATION TERMINEE : TOUTES VILLES CONFONDUES")
    print("="*80)


if __name__ == "__main__":
    main()