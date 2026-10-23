# LANDUSE-US-001 · Check what land use restrictions mean for building on a plot

> **LANDUSE-US-001** · state **Proposed** · traces to [LANDUSE.01](../specifications/LANDUSE.01-restriction-check.md)

**As** a landowner planning to build a house
**I want** to see the restrictions on my plot by its cadastral number, together with what each of them means for building, on what legal basis, and whose consent I need
**so that** I know before applying for a building permit whether, where and under which conditions I am allowed to build.

## Context

Many plots in Estonia are covered by restrictions: protection zones of power
lines, gas pipes and roads, shoreline building exclusion zones, heritage and
nature protection areas. They come from many different owners and laws, and the
Land and Spatial Board collects them in one place — its restrictions information
system (KPOIS), published over X-Road as `kyKitsendusedAvalik` (input: cadastral
number; output per restriction: type, name, legal basis, owner of the
restricting object, affected area in m²). Over the imitated registry in this
course, X-Road-shaped in production.

Finding the restrictions is already possible (the Land and Spatial Board's
Restrictions Application, explained in a separate help article for citizens).
What is missing is **understanding**: what is forbidden, what is allowed only
with consent, and whose consent is needed. This component adds a **rule
catalogue** that answers that, the same way for everyone, and lets the owner
ask the municipality before a formal application. One source of truth per fact:
the restrictions belong to the register and are looked up; the rules belong to
this component.

Out of scope: a map, login with ID-card / Mobile-ID / Smart-ID, submitting the
building permit or requesting the consent itself, a complete rule catalogue.
The public service hides sensitive restrictions (category I and II nature
protection zones); the owner is told the result may be incomplete.

Sources: [Land and Spatial Board help article](https://geoportaal.maaruum.ee/est/abi-ja-juhised/andmed/kuidas-leida-mis-kitsendused-minu-katastriuksust-puudutavad/66/) ·
[KPOIS X-Road specification](https://kataster.ee/sites/default/files/documents/2022-10/KPOIS%20X-tee%20spetsifikatsioon.pdf) ·
[X-Road/API services overview](https://www.kataster.ee/avaandmed/x-teeapi-teenused)

## Acceptance criteria

- A cadastral number in the wrong format (not `NNNNN:NNN:NNNN`) is refused with a clear message.
- For a valid cadastral number, the owner sees every restriction returned by the register: type, name, owner of the restricting object, legal basis, affected area in m² and as a share of the plot.
- An unknown cadastral number gives a clear "not found" message.
- When the register does not answer, the owner is told so and can retry; the application does not freeze.
- Next to each restriction the owner sees the matching rule: what is forbidden, what is allowed only with consent, whose consent is needed, and the legal basis.
- A restriction type with no rule yet is shown with the register's legal basis and the note "explanation not available".
- A rule editor can add, change and retire rules; each rule has a validity period, so it is always known which rule was shown at the time of a check.
- The owner can save the check as a pre-check with a description of the planned building and a contact e-mail; saving twice creates one pre-check; it gets the status "new" and a reference number.
- A planning officer can list pre-checks, filter them by status, and answer one (status "answered" with a comment).
