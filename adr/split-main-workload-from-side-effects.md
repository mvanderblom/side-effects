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

What is the change that we're proposing and/or doing?



## Consequences

What becomes easier or more difficult to do because of this change?