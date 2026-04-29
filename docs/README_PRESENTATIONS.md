# Presentation Guide

This guide explains what each PPT in this folder covers, why it exists, and when to use it.

## Start Here

- If you want the **main team session deck**, use `Team_AI_Tools_Evolution_Awareness_Session.pptx`.
- If you want a **short leadership briefing**, use `Executive_AI_Context_Presentation.pptx`.
- If you want **all source content combined**, use `Consolidated_AI_Context_Presentation.pptx`.
- If you want the **original source decks**, use the three original PPTs listed below.

## Presentation Catalog

- `Team_AI_Tools_Evolution_Awareness_Session.pptx`
  - **Purpose**: Primary awareness/training session for team adoption readiness.
  - **Context**: Explains AI tool evolution (Gen1, Gen2, Gen3), current org state (Copilot limits), role-based free tools, System Analyst and Business Analyst use cases, roadmap, risks, and action plan.
  - **Audience**: Engineering teams, System Analysts, Business Analysts, delivery leads.
  - **Use when**: Running workshops, team enablement sessions, or sprint kickoff awareness discussions.
  - **Notes**: Includes speaker notes on all slides.

- `Executive_AI_Context_Presentation.pptx`
  - **Purpose**: Condensed executive-level view.
  - **Context**: Quick narrative of why change is needed, what is changing, and what next.
  - **Audience**: Managers, senior stakeholders, leadership updates.
  - **Use when**: Time-limited reviews (10-20 minutes) or status/decision meetings.

- `Consolidated_AI_Context_Presentation.pptx`
  - **Purpose**: Full-context merged reference deck.
  - **Context**: Combines content from all original decks into one traceable sequence.
  - **Audience**: Anyone needing complete historical/contextual coverage.
  - **Use when**: Deep-dive prep, archival reference, or comprehensive onboarding.

- `AI_Workflow_Blueprint.pptx`
  - **Purpose**: Source deck focused on AI workflow concepts.
  - **Use when**: You need original material specific to workflow blueprinting.

- `From_Snippets_to_Systems.pptx`
  - **Purpose**: Source deck focused on transition from isolated AI usage to system-level outcomes.
  - **Use when**: You want to explain evolution in working style and delivery model.

- `The_Modular_AI_Workbench.pptx`
  - **Purpose**: Source deck focused on modular/structured AI workbench patterns.
  - **Use when**: You want details on modular architecture and componentized enablement.

## Which PPT Should I Choose?

- **Team learning session** -> `Team_AI_Tools_Evolution_Awareness_Session.pptx`
- **Leadership summary** -> `Executive_AI_Context_Presentation.pptx`
- **Complete reference** -> `Consolidated_AI_Context_Presentation.pptx`
- **Original source context** -> one of the 3 original PPTs

## Regeneration Scripts

If slides need updates, use these scripts:

- `build_team_ai_awareness_session_ppt.py`
- `build_executive_presentation.py`
- `build_consolidated_presentation.py`

Run from this folder:

- `python build_team_ai_awareness_session_ppt.py`
- `python build_executive_presentation.py`
- `python build_consolidated_presentation.py`

## Maintenance Notes

- Keep this README updated whenever a new presentation is added or renamed.
- Prefer editing script sources and regenerating PPTs instead of manual one-off edits.
- Add date/version in slide footer if sharing externally.
