# Split main workload from side effects

## Status

Proposed

## Context

We have an endpoint (either a controller or listener) that has a main workload/purpose 
and also has to do some other work after the main workload has completed. 
For brevity, we'll call this other work "side effects". 

Some requirements:
 - The main thread should not be impacted by these side effects in any way
 - The main thread should complete even if the side effects aren't completed yet
 - Multiple side effects should be able to be executed concurrently
 - Side effects should be resillient to failure
   - If side effect fails due to another system not being present or responsive, it should be retried

## Decision

Use spring application events and event listeners.
Work is separated from the main thread by the applicationEventMulticaster.

## Consequences

**Pro's**
 - Very easy to understand
 - Very easy to use
   - Relatively easy to pass data from the main workload to the side effects
 - Debugging is simpler because the events are named and contain data
 - Not writing our own framework (use what we already have)
 
**Con's**
 - It's all in memory so what happens when instance is shut down
   - Could be acceptable? 
     - Current way of doing things is also "in memory"
     - Should then only be used for non-critical work
   - Maybe we could even persist the events somewhere if this is an actual problem