# Pomodoro Wear OS

Application Pomodoro autonome pour Pixel Watch 3 (et toute montre Wear OS 3+).  
Package : `com.ivantorbey.pomodoro.watch` — installation par adb uniquement, pas de Play Store.

---

## Installation sur la montre par débogage sans fil

### 1. Activer le débogage sans fil sur la montre

Sur la montre :
- **Paramètres → Système → À propos de la montre** → appuyer 7 fois sur **Numéro de build** pour activer les options développeur.
- **Paramètres → Options pour les développeurs → Débogage sans fil** → activer.
- Prendre note de l'**adresse IP** et du **port** affichés (ex. `192.168.1.42:5555`).

### 2. Appairer (première fois seulement)

```bash
adb pair <IP>:<PORT_PAIR>
# Entrer le code à 6 chiffres affiché sur la montre
```

### 3. Connecter

```bash
adb connect <IP>:<PORT_CONNECT>
# Vérifier :
adb devices
```

### 4. Compiler et installer

```bash
# Depuis la racine du projet :
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

L'app apparaît dans la liste des applications de la montre sous le nom **Pomodoro**.

---

## Ajouter la tuile au cadran

1. Appuyer longuement sur le cadran de la montre.
2. Faire glisser vers la gauche jusqu'à la vignette **+** (Ajouter des tuiles).
3. Trouver **Pomodoro** dans la liste et appuyer sur **Ajouter**.
4. La tuile est maintenant accessible en balayant depuis le cadran.

---

## Ajouter la complication au cadran

1. Appuyer longuement sur le cadran.
2. Appuyer sur **Modifier** (ou directement sur un emplacement de complication).
3. Choisir l'emplacement souhaité (court, long, valeur…).
4. Faire défiler jusqu'à **Pomodoro** et sélectionner.

La complication affiche :
- **Hors session** : 🍅 + durée de travail configurée.
- **En travail** : temps restant en minutes + anneau de progression (RANGED_VALUE).
- **En pause** : « Pause » + minutes restantes.
- **Pause terminée** : « Pause ✓ ».
- **Tap** : ouvre l'app sur l'écran en cours.

---

## Fonctionnement rapide

| Écran | Geste | Action |
|---|---|---|
| Accueil | Couronne | Modifie la durée (travail ou pause selon la sélection) |
| Accueil | Tap sur un champ | Sélectionne ce champ pour la couronne |
| Accueil | Appui long | Active / désactive le **mode test** (60 s / 60 s) |
| Travail | Bouton | Passe en pause immédiatement |
| Travail | Appui long | Écran de confirmation d'arrêt |
| Pause | Cases à cocher | Cocher les 3 items pour débloquer Reprendre |
| Pause | Appui long | Écran de confirmation d'arrêt |

---

## Architecture résumée

```
AlarmManager (RTC_WAKEUP)
    └─► AlarmReceiver   – transition de phase → vibration + notification
BootReceiver            – reprogramme ou avance la phase au reboot
DataStore<Preferences>  – état persisté (phase, endTimeMs, durées, tirage, cases)
PomodoroViewModel       – lit DataStore, expose StateFlow, dispatch actions
MainActivity            – AmbientLifecycleObserver + when(phase) → écran
OngoingActivity         – visible depuis le cadran pendant toute la session
```

Pas de foreground service : toute la fiabilité repose sur `AlarmManager.setExactAndAllowWhileIdle`.
