# Consolidation Decisions

- Canonical files are selected by first-seen order from repos sorted by recent push date.
- Default branch is processed before non-default branches inside each repository.
- Exact hash duplicates are not copied again; provenance is tracked in migration manifest.
- Same-path, different-content collisions are moved to archive conflict buckets.
