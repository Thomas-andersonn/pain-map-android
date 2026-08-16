# Product Requirements Document (PRD): PainMapAI
**3D Human Body Pain-Mapping & AI Clinical Triage System**

---

## 1. Vision & Objectives
**PainMapAI** enables patients and clinicians to capture high-fidelity anatomical pain data on an interactive 3D human body model and receive automated, preliminary clinical triage, red-flag risk alerts, and structured medical appointment summaries powered by Google Gemini AI.

---

## 2. Core Epics & Feature Specifications

### Epic 1: Interactive 3D Anatomical Body Mapping (SceneView / Filament)
- **US-1.1 (3D Navigation):** Smooth orbit, pan, pinch-to-zoom, and 360° rotation around a 3D anatomical human model.
- **US-1.2 (Anatomical Region Pinpointing):** Raycasting/tap detection on body meshes (Cervical, Thoracic, Lumbar, Shoulder, Elbow, Wrist, Hip, Knee, Ankle, Head/Cranium, etc.).
- **US-1.3 (Dynamic Heatmap Markers):** Render glowing 3D spheres/nodes on highlighted anatomical regions color-graded by severity (Mild: Green/Teal, Moderate: Amber, Severe: Red/Crimson).

### Epic 2: Granular Clinical Pain Logging (Material 3 BottomSheet)
- **US-2.1 (Pain Intensity):** Material 3 Slider with Visual Analog Scale (VAS 1–10).
- **US-2.2 (Pain Qualities / Sensation Types):** Multi-select FilterChips (Throbbing, Stabbing, Burning, Dull Ache, Shooting/Radiating, Numbness, Stiffness, Electric Shock).
- **US-2.3 (Temporal Pattern & Triggers):** Duration selector (Acute < 2 weeks, Subacute 2-12 weeks, Chronic > 3 months) and aggravating/relieving factors.
- **US-2.4 (Contextual Notes):** Free-form audio/text description of pain context.

### Epic 3: Gemini-Powered Clinical Triage & Red-Flag Alerts
- **US-3.1 (Structured Clinical Triage):** Send full anatomical pain payload to Gemini AI with structured schema enforcement (Severity, Potential Conditions to Discuss, Recommended Specialty, Urgency Level).
- **US-3.2 (Red-Flag Emergency Detection):** Automated warning banners when symptoms match emergency criteria (e.g., sudden severe headache, chest pressure radiating to arm, numbness with loss of motor control).
- **US-3.3 (Follow-up Refinement):** AI-generated clinical clarification questions to refine patient understanding.
- **US-3.4 (Doctor Consultation Export):** Generate structured, formatted clinical handover report.

### Epic 4: Historical Pain Timeline & Longitudinal Heatmaps
- **US-4.1 (Pain Diary):** Timeline list of past pain records with visual intensity indicators.
- **US-4.2 (Trend Analytics):** Visual progression of pain scores over time to measure treatment or therapy response.

---

## 3. Non-Functional & Clinical Safety Requirements
- **Safety Disclaimer:** Explicit Medical Disclaimer banner stating PainMapAI is an informational triage aid, not a definitive diagnosis.
- **Offline Reliability:** Full local caching of pain points and reports; graceful handling when offline.
- **Performance:** 60fps rendering in SceneView; AI response loading states within 3s.
- **Target OS:** Android 7.0+ (Min SDK 24, Target SDK 34).
