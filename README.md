<div align="center">

# 🧑‍🤝‍🧑 DiviFriends

**Divide gastos entre amigos, sin cuentas ni servidores**

*Un producto de [Xyon Platforms](https://github.com/mkandreum)*

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)

</div>

---

## ¿Qué es DiviFriends?

DiviFriends es una app Android para repartir gastos de grupo: viajes, cenas, pisos compartidos... Apunta quién ha pagado qué, calcula automáticamente cuánto debe cada uno y propone el mínimo de pagos para quedar en paz. Funciona 100 % offline: todos los datos se guardan en el propio dispositivo, y los grupos se comparten acercando dos móviles por **NFC**.

## ✨ Funcionalidades

- **Grupos y participantes**: crea grupos en dos pasos, añade o quita participantes y edita el grupo en cualquier momento
- **Gastos**: añade, edita, elimina y filtra gastos de cada grupo
- **Saldos automáticos**: saldo por grupo y saldo general entre todos tus grupos
- **Pagos para saldar todo**: calcula los pagos mínimos necesarios y permite marcar deudas como saldadas
- **Estadísticas** del grupo con gráfico y **actividad reciente**
- **Invitaciones por NFC**: un móvil emula una tarjeta NFC (Host Card Emulation) con los datos del grupo y el otro la lee para unirse; si el NFC está apagado, la app te lleva a los ajustes para activarlo
- **Exportar**: grupo en CSV o como texto, y copiar un resumen para pegarlo en WhatsApp o donde quieras
- **Copias de seguridad**: guarda todos los datos en un archivo JSON y restáuralos después
- **Ajustes**: cambio de tema, asistente de bienvenida y opción para eliminar todos los datos

## 🛠️ Stack técnico

| Capa | Tecnología |
|------|----------|
| Interfaz | Kotlin + Jetpack Compose (Material 3) con tema glass/neón |
| Navegación | Navigation Compose (single-Activity) |
| Estado | `ViewModel` + `StateFlow` + `AppRepository` |
| Datos | JSON en almacenamiento interno (sin servidor, 100 % offline) |
| Gráficos | Canvas nativo (sin dependencias de CDN) |
| NFC | `HostApduService` (AID `F0010203040506`) + `ReaderMode` con APDU/Ndef |

Requiere un dispositivo con NFC HCE. minSdk 27, targetSdk 36, Java 17.

## 🚀 Compilar

```bash
git clone https://github.com/mkandreum/DiviFriends.git
cd DiviFriends
./gradlew :app:assembleDebug      # debug
./gradlew :app:assembleRelease    # release
```

También se puede abrir directamente con Android Studio.

## 🏢 Xyon Platforms

DiviFriends es un producto desarrollado y mantenido por **Xyon Platforms**, empresa especializada en soluciones digitales para negocios locales y pymes.

> © Xyon Platforms — Todos los derechos reservados
