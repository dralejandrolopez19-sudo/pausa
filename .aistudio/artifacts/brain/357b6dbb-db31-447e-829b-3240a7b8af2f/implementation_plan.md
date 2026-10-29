# Plan de Rediseño Sereno (Cero Ruido Visual) + Modo Oscuro "Bosque Nocturno"

## 1. Diagnóstico del Ruido Visual Actual
Actualmente, la pantalla de **Inicio** acumula demasiados elementos que compiten entre sí y rompen la calma de **Pausa**:
1. **Temporizador duplicado en Inicio (`FocusedWorkTimerCard`)**: Tener un reloj con selectores de trabajo/pausa (25/45/60/90 min y 5/10/15/30 min) justo encima del botón **"Necesito una pausa"** duplica la función de la pantalla dedicada **Modo Pausa** y genera confusión sobre dónde iniciar una pausa.
2. **Banner de estado del bloqueador demasiado grande**: Ocupa una tarjeta completa de 3 líneas en la parte superior incluso cuando el escudo ya está activo.
3. **Barra inferior decorativa redundante (`Mide • Limita • Pausa • Crece`)**: Añade peso visual debajo del botón principal sin aportar una acción útil.

---

## 2. Solución de Diseño: Inicio Sereno y Pausa Unificada

### A. Limpieza Profunda de la Pantalla de Inicio (`InicioScreen.kt`)
- **Eliminar el temporizador incrustado (`FocusedWorkTimerCard`) de Inicio**: En la pantalla de Inicio no habrá relojes ni selectores de intervalos corriendo.
- **Un único punto de acción hacia la pausa**: El botón principal **"Necesito una pausa"** (`btn_necesito_una_pausa`) será la entrada clara y directa hacia **Modo Pausa**.
- **Indicador de escudo compacto y discreto (`banner_estado_bloqueador_inicio`)**:
  - Cuando el bloqueador ya está activo, se muestra como una píldora sutil de una sola línea junto al saludo (sin ocupar una tarjeta grande).
  - Solo si falta activar permisos se muestra una tarjeta compacta con el botón directo **"Activar"**.
- **Eliminar el pie decorativo (`Mide • Limita • Pausa • Crece`)**: Deja que el botón **"Necesito una pausa"** cierre la pantalla con aire y limpieza visual.
- **Jerarquía visual clara y respirable en Inicio**:
  1. Encabezado (`PAUSA` + insignia `Nivel X`) y saludo sereno (`Hola, <Nombre>`).
  2. Tarjeta principal **"Tiempo hoy"** (progreso limpio frente a tu meta diaria).
  3. Fila de 3 métricas sintéticas (**Racha**, **Tiempo recuperado**, **Ahorro**) con textos concisos.
  4. Tarjeta **"Tu planta"** (vista previa limpia con acceso al jardín 3D).
  5. Botón principal **"Necesito una pausa"**.

### B. Unificación de Temporizadores en `ModoPausaScreen.kt`
- **Todo el control del tiempo vive en `Modo Pausa`**:
  - La pantalla **Modo Pausa** mantiene su círculo de respiración sereno y suma un selector superior simple de dos modos:
    - **Pausa consciente** (desconexión directa: `5 min`, `15 min`, `30 min`, `1 h`).
    - **Ciclo de enfoque** (bloque de concentración + pausa reparadora, p. ej. `25m / 5m` o `45m / 10m`, que al completarse suma tiempo recuperado y nutre tu planta).
  - Así conservamos toda la funcionalidad de intervalos de enfoque y pausa, pero integrada en su lugar natural sin saturar el Inicio.

---

## 3. Modo Oscuro "Bosque Nocturno" (`Ajustes` + Toda la App)

### A. Paleta "Bosque Nocturno" Reactiva (`Color.kt` y `Theme.kt`)
- Convertir `isPausaDarkThemeActive` en estado reactivo (`mutableStateOf`) para que cualquier cambio en el interruptor **`switch_modo_oscuro`** de **Ajustes** actualice al instante todas las pantallas y gráficos.
- **Tonos Bosque Nocturno**:
  - **Fondo principal**: Verde bosque profundo (`#121A15`).
  - **Tarjetas y superficies**: Verde pino suave (`#1B2620`) y superficie elevada (`#223029`) con bordes sutiles (`#2C3E34`).
  - **Acentos**: Salvia luminoso (`#68A37C` / `#85B896`) y texto crema cálido (`#EAE6DF`) de descanso visual.
- **Barras del sistema Android**: Ajuste automático de iconos claros/oscuros en la barra de estado y navegación con `WindowInsetsControllerCompat`.

### B. Adaptación de Gráficos 3D y Controles
- **Planta 3D (`Plant3DView.kt` y `TuPlantaScreen.kt`)**: Aura nocturna en tonos bosque (`#1E392B` → `#12231A`) y barras de progreso adaptadas al tema.
- **Controles y switches (`LimitesScreen.kt`, `PausaVisuals.kt`)**: Reemplazo de grises claros fijos por tokens dinámicos del tema.

---

## 4. Verificación
- Compilación limpia del proyecto (`compile_applet`) sin advertencias.
- Pruebas unitarias y Robolectric actualizadas (`gradle :app:testDebugUnitTest`) verificando que:
  1. La pantalla de Inicio muestra una jerarquía limpia sin temporizadores duplicados.
  2. La pantalla de Modo Pausa unifica las pausas conscientes y los bloques de enfoque.
  3. El interruptor de Modo Oscuro en Ajustes actualiza el tema "Bosque Nocturno" de inmediato y persiste la preferencia del usuario.
