# FinGuard Android

Dashboard de control de ventas para negocios que usan Yape. La aplicación captura notificaciones de Yape y las envía a un backend para análisis y visualización de estadísticas.

## Arquitectura

El proyecto sigue **Clean Architecture** con **MVVM**:

```
app/src/main/java/com/jose/holamundo/
├── core/                    # Utilidades y configuración
│   ├── config/              # AppConfig (BuildConfig wrapper)
│   ├── di/                  # Módulos de Hilt
│   └── network/             # Configuración de Ktor
├── data/                    # Capa de datos
│   ├── remote/              # APIs y DTOs
│   └── repository/          # Implementaciones de repositorios
├── domain/                  # Capa de dominio
│   └── model/               # Modelos de negocio
├── presentation/            # Capa de presentación
│   ├── dashboard/           # Pantalla de dashboard
│   ├── home/                # Pantalla principal
│   ├── navigation/          # Navegación
│   └── settings/            # Pantalla de ajustes
├── service/                 # Servicios Android
│   └── YapeNotificationListener.kt
└── ui/                      # Design System
    ├── components/          # Componentes reutilizables
    └── theme/               # Colores, tipografía, tema
```

## Tecnologías

- **Kotlin** + **Jetpack Compose** - UI declarativa
- **Hilt** - Inyección de dependencias
- **Ktor Client** - HTTP client
- **Navigation Compose** - Navegación entre pantallas
- **Kotlinx Serialization** - Serialización JSON

## Variables de Entorno

Las variables de configuración se definen en `app/build.gradle.kts`:

```kotlin
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
        buildConfigField("Boolean", "ENABLE_LOGS", "true")
    }
    release {
        buildConfigField("String", "BASE_URL", "\"https://api.finguard.com\"")
        buildConfigField("Boolean", "ENABLE_LOGS", "false")
    }
}
```

Accede a ellas mediante `AppConfig`:

```kotlin
val url = AppConfig.baseUrl
val enableLogs = AppConfig.enableLogs
```

## Configuración para desarrollo local

1. **Emulador**: La URL por defecto `http://10.0.2.2:8080` apunta al localhost de tu máquina.

2. **Dispositivo físico**: Modifica `BASE_URL` en el `build.gradle.kts` a la IP de tu máquina en la red local (ej: `http://192.168.1.100:8080`).

## Permisos requeridos

- `INTERNET` - Para comunicación con el backend
- `POST_NOTIFICATIONS` - Para notificaciones de prueba
- `BIND_NOTIFICATION_LISTENER_SERVICE` - Para capturar notificaciones de Yape

## Cómo ejecutar

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease
```

## Estructura de navegación

La app usa **Bottom Navigation** con tres pantallas:

1. **Home** - Estado del servicio y controles
2. **Dashboard** - KPIs y estadísticas de ventas
3. **Settings** - Configuración de la app
