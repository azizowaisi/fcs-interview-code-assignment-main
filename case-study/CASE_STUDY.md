# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
- **Challenges:** Splitting shared costs (e.g. transport used by several warehouses), different cost drivers per site, and timing (accrual vs cash). Allocating overhead (IT, security) fairly without arbitrary rules is hard.
- **Considerations:** Define allocation keys (e.g. by volume, weight, or headcount) and document them. Use a cost centre per warehouse/store and a clear chart of accounts. Track direct costs at source and allocate indirect costs in a repeatable way.
- **What I’d need:** Current cost structure (fixed vs variable), how finance currently allocates costs, and whether we need to align with existing group reporting.

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
- **Strategies:** Consolidate shipments and optimize routes; right-size inventory to cut holding cost; standardize processes to reduce labour variance; automate where ROI is clear (e.g. putaway, cycle counts).
- **Prioritisation:** Use a matrix of impact vs effort; tackle quick wins first (e.g. better slotting, carrier renegotiation) while building data for bigger bets (automation, network changes).
- **Implementation:** Pilot in one warehouse or one flow; measure baseline and target (cost per unit, lead time); then roll out with change management. Revisit priorities as data improves.

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
- **Benefits:** Single source of truth for cost; faster closing; fewer manual re-entries; audit trail. Finance can report and analyse without re-keying from fulfilment systems.
- **Seamless integration:** Prefer APIs or event-driven sync over batch file drops. Agree on master data (cost centres, accounts, UoM) and a small set of key metrics (e.g. cost per order, cost per m³). Include reconciliation and exception handling (e.g. unmatched records, duplicates).
- **What I’d clarify:** Which system is the ledger of record, refresh frequency (real-time vs daily), and who owns mapping (fulfilment cost types to GL accounts).

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
- **Importance:** Budgets and capacity plans need a forward view; forecasting helps flag over/under spend and supports hiring and capex decisions.
- **Design:** Use historical trends, seasonality, and planned events (promos, new SKUs). Keep a simple baseline (e.g. rolling average) and layer on driver-based adjustments. Store assumptions (growth %, inflation) so they can be updated. Separate forecast from target so we don’t confuse “what we expect” with “what we aim for”.

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
- **Why preserve cost history:** The old warehouse’s costs are needed for variance analysis, audits, and to compare the new site’s performance (e.g. cost per unit before vs after). Reusing the same business unit code (as in our replace flow) keeps the lineage; we should keep cost records linked to that code and mark period (e.g. archived vs active).
- **Staying within budget:** Set a budget for the new warehouse from day one (based on plan and benchmarks). Track run-rate vs budget and compare to the archived warehouse’s last 12 months to validate that the replacement is on track.

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
