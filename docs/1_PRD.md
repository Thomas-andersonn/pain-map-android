# Product Requirements Document (PRD): PainMapAI
**3D Joint & Muscle Pain-Mapping & Root Cause AI Triage System**

---

## 1. Vision & Core Objective
**PainMapAI** is focused specifically on identifying and assessing the **root causes of Joint and Muscle Pain** (musculoskeletal strain, postural dysfunction, myofascial trigger points, tendinopathy, ligamentous tension, and biomechanical overuse). 

By mapping localized symptoms on an interactive 3D human body model, users can capture movement triggers, load sensitivity, and joint stiffness, and receive structured AI triage aimed at pinpointing the musculoskeletal root cause and actionable physical therapy/ergonomic next steps.

---

## 2. Targeted Clinical Scope (Joint & Muscle Pain Only)
- **Included Focus Areas:**
  - **Spine & Core:** Cervical strain, thoracic tightness, lumbar facet/disc mechanical stress, SI joint irritation.
  - **Upper Extremity Joints & Muscles:** Rotator cuff, deltoid myofascial tension, bicep/tricep tendinitis, tennis/golfer elbow, wrist carpal/extensor strain.
  - **Lower Extremity Joints & Muscles:** Hip impingement/gluteal amnesia, quadriceps/hamstring strain, patellofemoral knee pain, meniscus irritation, Achilles tendinitis, plantar fascia strain.
- **Excluded:** Internal organ pathologies, systemic diseases, infectious illnesses, cardiovascular conditions.

---

## 3. Core Epics & Feature Specifications

### Epic 1: Interactive 3D Musculoskeletal Body Map (SceneView / Filament)
- **US-1.1 (3D Joint & Muscle Inspection):** Smooth 360° rotation, pinch-zoom, and anatomical focus on major joint complexes (Neck, Shoulders, Spine, Hips, Knees, Ankles).
- **US-1.2 (Joint & Muscle Point Selection):** Pinpoint specific joint lines, tendon attachments, or muscle bellies.
- **US-1.3 (Severity Heatmap Markers):** Color-graded visual nodes (Mild: Green/Teal, Moderate: Amber, Severe: Red/Crimson).

### Epic 2: Musculoskeletal Pain Detail Logging (Material 3 BottomSheet)
- **US-2.1 (Intensity):** 1–10 Visual Analog Scale (VAS).
- **US-2.2 (Musculoskeletal Sensations):** Specialized descriptors (Dull Muscle Ache, Sharp Joint Pinch, Stiffness, Burning Tendon Sensation, Muscle Spasm/Tightness, Radiating Nerve Tension).
- **US-2.3 (Biomechanical Triggers):** Prolonged sitting, heavy lifting, repetitive overhead reach, running/impact, cold weather, morning stiffness.
- **US-2.4 (Duration & Onset):** Acute (recent strain), Subacute (lingering overuse), Chronic (>3 months postural/biomechanical imbalance).

### Epic 3: Gemini-Powered Joint & Muscle Root Cause Triage
- **US-3.1 (Root Cause Analysis):** Synthesize anatomical distribution into probable musculoskeletal mechanisms (e.g., "Patellofemoral tracking imbalance driven by quadriceps tightness").
- **US-3.2 (Targeted Biomechanical Questions):** AI clarifies aggravating movements (e.g., "Does pain worsen when going down stairs or after prolonged sitting?").
- **US-3.3 (Clinical Handover & Specialist Guide):** Recommends specific physical therapy disciplines, orthopedic specialties, and targeted stretching/mobility self-care.

---

## 4. Non-Functional Requirements
- **Performance:** 60fps rendering in SceneView; AI response loading states with progress indicators.
- **Target OS:** Android 7.0+ (Min SDK 24, Target SDK 34).
