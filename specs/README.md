# Specifications

This directory contains the source of truth for the game-price-comparator system.

## Specification Files

| File | Description |
|------|-------------|
| `01-product-overview.md` | Project goals, non-functional requirements, target users |
| `02-architecture.md` | System architecture, layers, components, data flow |
| `03-domain-model.md` | Entities, relationships, enumerations, indexes |
| `04-api.md` | API endpoints, request/response formats, error handling |

## Development Workflow

This project follows a **spec-driven development** approach:

### 1. Update Specifications First
Before making any code changes, update the relevant spec file(s) in this directory. Changes may include:
- New features or requirements
- API contract changes
- New entities or modifications to existing ones
- Architecture decisions

### 2. Review Specifications
Team members review the specifications to ensure:
- Requirements are clear and complete
- Technical decisions are sound
- API contracts are well-defined

### 3. Implement Changes
Once specs are approved, implement the changes in code:
- Code must match the specifications exactly
- If implementation reveals issues, update specs first
- Do not deviate from specs without explicit approval

### 4. Verify Implementation
- Ensure code compiles without errors
- Run tests to verify functionality
- Check that API contracts match spec documentation

## Key Principles

- **Single Source of Truth**: The specs in this directory are the authoritative source for all system behavior
- **Traceability**: Any code change should be traceable to a specification
- **Documentation**: Keep specs updated - they serve as documentation for the entire team
- **Collaboration**: Review specs together before implementing

## Example Workflow

```
1. Need to add a new store (e.g., Epic Games Store)
   ↓
2. Update 02-architecture.md to document the new collector
   ↓
3. Update 03-domain-model.md if new fields are needed
   ↓
4. Review the changes with the team
   ↓
5. Implement EpicGamesCollector implementing StoreCollector interface
   ↓
6. Verify everything works
```

## Questions?

If you have questions about the specifications or need clarification, start by reading the relevant spec file. For architectural decisions, refer to `02-architecture.md`.