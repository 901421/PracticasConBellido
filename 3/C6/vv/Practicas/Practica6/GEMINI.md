# Project Mandates

## Cucumber Feature Organization
- Each Cucumber `.feature` file MUST correspond to exactly one **Use Case** or **Requirement**.
- Before defining the scenarios in a feature file, **Equivalence Partitioning** (Particiones de Equivalencia) techniques MUST be applied to identify the representative test cases.

## Testing Conventions
- Prioritize `Scenario Outline` with `Examples` when multiple equivalence partitions are identified for the same behavior.
- Ensure that the feature name and description clearly reflect the use case or requirement being tested.
