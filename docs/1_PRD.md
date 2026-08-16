# Product Requirements Document (PRD): PainMapAI

## 1. Product Overview
**PainMapAI** is a modern, clinical-grade Android application designed to revolutionize pain tracking and symptom analysis. By combining an interactive 3D anatomical body model (powered by SceneView) with generative AI (Google Gemini AI), users can visually pinpoint pain locations, describe sensations with high fidelity, track pain progressions over time, and generate actionable medical summaries for healthcare providers.

---

## 2. Target Users & Problem Statement
- **Chronic Pain Patients:** Suffer from recurring or shifting pain (fibromyalgia, arthritis, migraines) and struggle to accurately describe location and intensity changes to clinicians.
- **Post-Surgical / Injury Rehabilitation:** Need precise anatomical logging of healing or aggravating areas over days and weeks.
- **Healthcare Providers:** Require structured, objective pain history reports rather than vague patient recollections.

---

## 3. Core Epics & Feature Specifications

### Epic 1: Interactive 3D Anatomical Body Map
- **US-1.1:** As a user, I want to rotate, zoom, and pan a 3D anatomical model of the human body (front/back/lateral) to locate my pain area.
- **US-1.2:** As a user, I want to tap on specific body regions (head, cervical spine, lumbar, knee, shoulder, etc.) to place a visual 3D pain marker.
- **US-1.3:** As a user, I want pain markers to visually reflect pain severity using color-coded heatmap indicators (Green: 1-3, Amber: 4-6, Red: 7-8, Crimson: 9-10).

### Epic 2: Granular Pain Point Logging
- **US-2.1:** As a user, I want to specify pain intensity on a 1-10 visual analog scale (VAS).
- **US-2.2:** As a user, I want to classify pain sensations (Throbbing, Stabbing, Aching, Burning, Tingling, Numbness, Stiffness).
- **US-2.3:** As a user, I want to record pain triggers, duration, and contextual notes.

### Epic 3: Gemini AI Symptom Intelligence
- **US-3.1:** As a user, I want to receive an instant, empathetic AI assessment analyzing the anatomical pattern and characteristics of my logged pain points.
- **US-3.2:** As a user, I want Gemini AI to suggest relevant follow-up questions to refine the assessment (e.g., "Does pain radiate down the leg?").
- **US-3.3:** As a user, I want to generate an exportable clinical summary formatted for doctor consultations.

### Epic 4: Pain History & Trend Analytics
- **US-4.1:** As a user, I want to view a timeline and history log of past pain entries.
- **US-4.2:** As a user, I want to see pain intensity trends over days/weeks to assess treatment effectiveness.

---

## 4. Technical Constraints & Non-Functional Requirements
- **Platform:** Android 7.0+ (Min SDK 24, Target SDK 34).
- **UI:** 100% Jetpack Compose with Material Design 3.
- **Offline First:** Local persistence for all pain logs with optional cloud/AI sync.
- **Performance:** 60fps rendering in 3D viewport, fast cold start under 1.5s.
- **Privacy & Security:** Anonymized local storage of health data; API keys stored securely.
