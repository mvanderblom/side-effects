package dev.vanderblom.intergamma.sideeffects.events

import dev.vanderblom.intergamma.sideeffects.BusinessService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class EventsExampleController (
  private val businessService: BusinessService,
  private val eventPublisher: ApplicationEventPublisher
) {

  fun someEndpoint() {
    businessService.mainWorkload()
    eventPublisher.publishEvent(SideEffectEvent())
    eventPublisher.publishEvent(SideEffectEvent())
  }

  fun endpointWithBadSideEffect() {
    businessService.mainWorkload()
    eventPublisher.publishEvent(BadSideEffectEvent())
    eventPublisher.publishEvent(SideEffectEvent())
  }

  fun endpointWithSideEffectsThatTakeTime() {
    businessService.mainWorkload()
    eventPublisher.publishEvent(ExpensiveSideEffectEvent())
    eventPublisher.publishEvent(ExpensiveSideEffectEvent())
  }
}

