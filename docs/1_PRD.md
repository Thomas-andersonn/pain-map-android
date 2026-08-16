# Product Requirements Document (PRD): PainMapAI
**3D Human Body Pain-Mapping & AI Clinical Triage System (Prototype)**

---

## 1. Vision & Objectives
**PainMapAI** enables users to visually pinpoint and log anatomical pain on an interactive 3D human body model and receive automated, structured preliminary clinical triage and medical consultation summaries powered by Google Gemini AI.

---

## 2. Core Epics & Feature Specifications (Prototype Scope)

### Epic 1: Interactive 3D Anatomical Body Mapping (SceneView / Filament)
- **US-1.1 (3D Navigation):** Smooth orbit, pan, pinch-to-zoom, and 360° rotation around a 3D anatomical human model.
- **US-1.2 (Anatomical Region Pinpointing):** Tap/selection on body regions (Head/Cranium, Neck/Cervical, Upper Back, Lower Back/Lumbar, Chest, Shoulder, Elbow, Wrist, Hip, Knee, Ankle/Foot).
- **US-1.3 (Dynamic Heatmap Markers):** Render visual 3D indicator nodes on highlighted anatomical regions color-graded by severity (Mild: Green/Teal, Moderate: Amber, Severe: Red/Crimson).

### Epic 2: Granular Clinical Pain Logging (Material 3 BottomSheet)
- **US-2.1 (Pain Intensity):** Material 3 Slider with Visual Analog Scale (VAS 1–10).
- **US-2.2 (Pain Qualities / Sensation Types):** Multi-select FilterChips (Throbbing, Stabbing, Burning, Dull Ache, Shooting/Radiating, Numbness, Stiffness, Electric Shock).
- **US-2.3 (Temporal Pattern & Triggers):** Duration selector (Acute < 2 weeks, Subacute 2-12 weeks, Chronic > 3 months) and aggravating/relieving factors.
- **US-2.4 (Contextual Notes):** Free-form text description of pain context.

### Epic 3: Gemini-Powered Clinical Triage & Consultation Report
- **US-3.1 (Structured Clinical Triage):** Send full anatomical pain payload to Gemini AI with structured schema enforcement (Severity, Potential Conditions to Discuss, Recommended Specialty, Urgency Level).
- **US-3.2 (Clinical Follow-up Questions):** AI-generated clinical clarification questions to refine patient understanding.
- **US-3.3 (Doctor Consultation Report):** Generate structured, formatted clinical summary report ready for sharing.

---

## 3. Non-Functional Requirements
- **Offline Reliability:** Graceful fallback if Gemini API is unreachable.
- **Performance:** 60fps rendering in SceneView; AI response loading states with progress indicators.
- **Target OS:** Android 7.0+ (Min SDK 24, Target SDK 34).
