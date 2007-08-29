package scala.specs.matchers
import scala.specs.integration._

object mocksSpec extends Specification { "Mocks" areSpecifiedBy (mockProtocols) }
object mockProtocolsSuite extends JUnit3(mockProtocols) 
object mockProtocols extends MatchersSpecification with ButtonAndLightMock {
  "Mock protocols" should { usingBefore { () => {clearExample; button.init()} }
    "provide an 'expect inAnyOrder' protocol checking if calls have been made to mock objects" in {
      // by default, the calls can be made in any order
      val protocol = expect { mock.on; mock.off }
      assertion(protocol must beMet) must (failWithMatch("Expected in any order \\[on\\(.*\\); off\\(.*\\)\\]"))

      button.push
      assertion(protocol must beMet) must failWithMatch("Expected in any order \\[on\\(.*\\); off\\(.*\\)\\]")

      button.push  // the protocol is always checked at the end of an example
    }
    "provide an 'expect inSequence' protocol checking if calls have been made to mock objects inSequence" in {
      var protocol = expect(inSequence) { mock.off; mock.on }
      2.times {i => button.push}
      assertion(protocol must beMet) must failWithMatch("Failed protocol. Received:\n  on\\(.*\\)\n  off\\(.*\\)")

      protocol = expect(inSequence) { mock.on; mock.off }
      2.times {i => button.push}
    }
    "provide an 'expect anyOf' protocol authorizing any of a set of calls to be made" in {
      var protocol = expect(anyOf) { mock.destroy }
      button.pound
    }
    "provide an 'expect oneOf' protocol checking if one call exactly has been made" in {
      var protocol = expect(oneOf) { mock.on; mock.off }
      assertion(protocol must beMet) must failWithMatch("Expected 1 of: \\[on\\(.*\\); off\\(.*\\)\\]. Received none")

      protocol = expect(oneOf) { mock.on; mock.off }
      3.times {i => button.push}
      assertion(protocol must beMet) must failWithMatch("Expected 1 of: \\[on\\(.*\\); off\\(.*\\)\\]. Received:\n  on\\(.*\\)\n  off\\(.*\\)\n  on\\(.*\\)")

      button.init()
      protocol = expect(oneOf) { mock.on; mock.off }
      2.times {i => button.push}
    }
    "provide an 'expect atLeastOneOf' protocol checking if at least one call has been made" in {
      var protocol = expect(atLeastOneOf) { mock.on; mock.off }
      assertion(protocol must beMet) must failWithMatch("Expected at least 1 of: \\[on\\(.*\\); off\\(.*\\)\\]. Received none")

      protocol = expect(atLeastOneOf) { mock.on; mock.off }
      2.times {i => button.push}
    }
    "provide an 'expect atMostOneOf' protocol checking if at most one call has been made" in {
      var protocol = expect(atMostOneOf) { mock.on; mock.off }
      4.times {i => button.push}
      assertion(protocol must beMet) must failWithMatch("Expected at most 1 of: \\[on\\(.*\\); off\\(.*\\)\\]. Received:\n  on\\(.*\\)\n  off\\(.*\\)")

      protocol = expect(atMostOneOf) { mock.on; mock.off }
      2.times {i => button.push}
    }
    "provide an 'expect n.of' protocol checking if exactly n calls have been made" in {
      var protocol = expect(2.of) { mock.on; mock.off }
      assertion(protocol must beMet) must failWithMatch("Expected 2 of: \\[on\\(.*\\); off\\(.*\\)\\]. Received none")

      protocol = expect(2.of) { mock.on; mock.off }
      4.times {i => button.push}
    }
    "provide an 'expect at least n of' protocol checking if exactly n calls have been made" in {
      var protocol = expect(3.atLeastOf) { mock.on; mock.off }
      assertion(protocol must beMet) must failWithMatch("Expected at least 3 of: \\[on\\(.*\\); off\\(.*\\)\\]. Received none")

      protocol = expect(2.atLeastOf) { mock.on; mock.off }
      6.times {i => button.push}
    }
  }
  "Mock protocols" can { usingBefore { () => {clearExample; button.init()} }
  "be nested to allow complex expectations: expect, inAnyOrder 1 'on' and 2 'off'" in {
      val protocol = expect(inAnyOrder) { 
        1.of {mock.on; mock.off; mock.on} 
        1.of {mock.off} 
      }
      assertion(protocol must beMet) must failWithMatch(".*Expected in any order \\[1 of: \\[on\\(.*\\); off\\(.*\\); on\\(.*\\)]; 1 of: \\[off\\(.*\\)\\]\\]. Received none")

      2.times {i => button.push}
      assertion(protocol must beMet) must failWithMatch(".*Expected in any order \\[1 of: \\[on\\(.*\\); off\\(.*\\); on\\(.*\\)\\]; 1 of: \\[off\\(.*\\)\\]\\]. Received.*")
      
      protocol.clear
      button.init()
      2.times {i => button.push}
    }
  }
}
trait ButtonAndLightMock extends ButtonAndLight with Mocker {
  val mock = new Light { 
    override def on = record 
    override def off = record
    override def destroy = record
  }
  val button = Button(mock)
}
trait ButtonAndLight {
  case class Button(light: Light) {
    var lightOn = false
    def push = {
      if (lightOn) light.off else light.on 
      lightOn = !lightOn
    }
    def pound = light.destroy
    def init() = lightOn = false
  }
  case class Light {
    var state: LightState = Off
    def on = state = On
    def off = state = Off
    def isOn = state == On
    def destroy = {}
  }
  abstract sealed class LightState(s: String)
  case class On extends LightState("on")
  case class Off extends LightState("off")
}
