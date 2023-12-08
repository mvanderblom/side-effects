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

## Decision

Create a service that uses scheduling as a trigger to execute side effects.


## Consequences

Work is separated from the main thread because the scheduled function
is called from another thread.

**Pro's**
 - easy to implement
 - easy to understand

**Con's**
 - It's all in memory so what happens when instance is shut down
   - Could be that this is ok? 
     - Current way of doing things is also "in memory"
     - This way increases the wait time though
   - Should then only be used for non-critical work.

What becomes easier or more difficult to do because of this change?