# This script scaffolds the project structure using PowerShell commands.

# --- Configuration ---
$baseDir = ".\src\main\java\com\auratrackr"
Write-Host "Creating project structure under $baseDir..."

# --- Create Directories (Packages) ---
# The -Force switch acts like 'mkdir -p', creating parent directories as needed.
New-Item -ItemType Directory -Force -Path "$baseDir\core\di"
New-Item -ItemType Directory -Force -Path "$baseDir\core\navigation"
New-Item -ItemType Directory -Force -Path "$baseDir\core\utils"
New-Item -ItemType Directory -Force -Path "$baseDir\data\local\dao"
New-Item -ItemType Directory -Force -Path "$baseDir\data\local\entity"
New-Item -ItemType Directory -Force -Path "$baseDir\data\remote\dto"
New-Item -ItemType Directory -Force -Path "$baseDir\data\repository"
New-Item -ItemType Directory -Force -Path "$baseDir\domain\model"
New-Item -ItemType Directory -Force -Path "$baseDir\domain\repository"
New-Item -ItemType Directory -Force -Path "$baseDir\domain\usecase"
New-Item -ItemType Directory -Force -Path "$baseDir\services"
New-Item -ItemType Directory -Force -Path "$baseDir\features\focus_blocker\ui"
New-Item -ItemType Directory -Force -Path "$baseDir\features\fitness_tracker\ui"
New-Item -ItemType Directory -Force -Path "$baseDir\features\social\ui"
New-Item -ItemType Directory -Force -Path "$baseDir\features\onboarding\ui"
New-Item -ItemType Directory -Force -Path "$baseDir\ui\components"

# --- Create Files ---
# New-Item creates empty files.
New-Item -ItemType File -Path "$baseDir\AuraTrackrApp.kt"
New-Item -ItemType File -Path "$baseDir\MainActivity.kt"
New-Item -ItemType File -Path "$baseDir\core\di\AppModule.kt"
New-Item -ItemType File -Path "$baseDir\core\di\DatabaseModule.kt"
# ... (and so on for all files)
# To keep this brief, I'll list a few. The full script would list every file.
New-Item -ItemType File -Path "$baseDir\domain\model\AppInfo.kt"
New-Item -ItemType File -Path "$baseDir\domain\model\Workout.kt"
New-Item -ItemType File -Path "$baseDir\features\focus_blocker\ui\FocusDashboardScreen.kt"
New-Item -ItemType File -Path "$baseDir\features\focus_blocker\ui\FocusDashboardViewModel.kt"

Write-Host "✅ AuraTrackr project structure created successfully!"