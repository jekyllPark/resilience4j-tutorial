# Resilience4j
Resilience4j(Resilience + For Java)는 함수형 프로그래밍으로 설계된 경량 장애 허용(```fault tolerance```) 라이브러리로, 서킷 브레이커 패턴을 위해 사용된다.

MSA 환경에서는 단일 마이크로서비스의 장애가 전체 시스템에 영향을 미치지 않도록 장애가 발생해도 견딜 수 있는 내결함성이 중요하다.

```fault tolerance```란 하나 이상의 노드에서 문제가 발생하더라도 시스템의 중단 없이 계속 작동할 수 있는 것을 의미한다.

자바 진영의 서킷 브레이커 라이브러리는 넷플릭스의 ```Histrix```도 있지만, 현재 스프링 진영에서 deprecated 되어 Resilience4j를 사용해야 한다.

# 구성 요소
## CircuitBreaker
일반적 서킷 브레이커의 상태에 맞게 유한 상태 기계를 구현한 모듈로, 실패할 수 있는 작업에 대한 프록시 역할을 하며, 최근 실패 수를 모니터링하고 이 정보를 사용해 작업의 지속 여부 또는 단순 예외를 즉시 반환할 지 결정한다.
![image](https://github.com/jekyllPark/resilience4j-tutorial/assets/114489012/34b89d06-f5f8-4e3b-a73a-3014d9e0418a)
- 닫힘(CLOSE)

  닫힘 상태일 때 정상적으로 작동(서비스를 통한 요청 통과)한다.
  그러나 오류가 임계값 제한을 초과하면 회로 차단기가 작동하며, 위의 다이어그램에서 볼 수 있듯이, 회로의 상태가'개방'으로 전환된다.
- 열림(OPEN)

  개방 상태일 때 들어오는 요청은 실제 작업을 실행하려는 시도 없이 오류(fallback)와 함께 반환된다.
- 반개방(HALF-OPEN)

  열림상태에서 일정 시간이 지나면 차단기가 half-open 상태가 되며, 이 상태에서 회로 차단기는 제한 된 수의 테스트 요청을 통과하도록 허용하고, 요청이 성공할경우 닫힌 상태로 돌아가며, 트래픽은 평소와 같이 통과된다. 반면 요청이 실패할 경우 다른 제한 시간이 초과될 때까지 열린 상태로 유지된다.

Resilience4j의 서킷 브레이커는 유한 상태 머신을 통해 구현되며, ```개수 기반 슬라이딩 윈도우```, ```시간 기반 슬라이딩 윈도우```를 통해 통화 결과를 저장, 집계한다.
> - Count-based sliding window
> 
>   메트릭 수집 방법 중하나로, 일정 개수의 요청을 추적하고, 해당 요청들 중 실패 요청의 비율을 계산하여 회로 차단을 결정한다.
>
> - Time-based sliding window
>   슬라이딩 윈도우의 길이를 정한 후 해당 기간 동안의 실패율을 계산하여 특정 임계치에 도달했는지를 계산한다.

### Property
| property | description |
| --- | --- |
| failureRateThreshold | 실패 비율 임계치를 백분율로 설정 해당 값을 넘어갈 시 Circuit Breaker 는 Open 상태로 전환되며, 이때부터 호출을 차단한다 (기본값: 50) |
| slowCallRateThreshold | 임계값을 백분율로 설정, CircuitBreaker는 호출에 걸리는 시간이 slowCallDurationThreshold보다 길면 느린 호출로 간주,해당 값을 넘어갈 시 Circuit Breaker 는 Open상태로 전환되며, 이때부터 호출을 차단한다 (기본값: 100) |
| slowCallDurationThreshold | 호출에 소요되는 시간이 설정한 임계치보다 길면 느린 호출로 계산.응답시간이 느린 것으로 판단할 기준 시간 (60초, 1000 ms = 1 sec) (기본값: 60000[ms]) |
| permittedNumberOfCallsInHalfOpenState | HALF_OPEN 상태일 때, OPEN/CLOSE 여부를 판단하기 위해 허용할 호출 횟수를 설정 수 (기본값: 10) |
| maxWaitDurationInHalfOpenState | HALF_OPEN 상태로 있을 수 있는 최대 시간이다. 0일 때 허용 횟수만큼 호출을 모두 완료할 때까지 HALF_OEPN 상태로 무한정 기다린다. (기본값: 0) |
| slidingWindowType | sliding window 타입을 결정한다. COUNT_BASED인 경우 slidingWindowSize 만큼의 마지막 call들이 기록되고 집계된다.TIME_BASED인 경우 마지막 slidingWindowSize초 동안의 call들이 기록되고 집계됩니다. (기본값: COUNT_BASED) |
| slidingWindowSize | CLOSED 상태에서 집계되는 슬라이딩 윈도우 크기를 설정한다. (기본값: 100) |
| minimumNumberOfCalls | minimumNumberOfCalls 이상의 요청이 있을 때부터 faiure/slowCall rate를 계산.예를 들어, 해당 값이 10이라면 최소한 호출을 10번을 기록해야 실패 비율을 계산할 수 있다.기록한 호출 횟수가 9번뿐이라면 9번 모두 실패했더라도 circuitbreaker는 열리지 않는다. (기본값: 100) |
| waitDurationInOpenState | OPEN에서 HALF_OPEN 상태로 전환하기 전 기다리는 시간 (60초, 1000 ms = 1 sec) (기본값: 60000[ms]) |
| recordExceptions | 실패로 기록할 Exception 리스트 (기본값: empty) |
| ignoreExceptions | 실패나 성공으로 기록하지 않을 Exception 리스트 (기본값: empty) |
| ignoreException | 기록하지 않을 Exception을 판단하는 Predicate을 설정 (커스터마이징, 기본값: throwable -> true) |
| recordFailure | 어떠한 경우에 Failure Count를 증가시킬지 Predicate를 정의해 CircuitBreaker에 대한 Exception Handler를 재정의.true를 return할 경우, failure count를 증가시키게 된다 (기본값: false) |

application.yml을 통한 서킷 브레이커 예제
```
#application.yml
resilience4j:
  circuit breaker:
    configs:
      default:
        failure-rate-threshold: 50
        minimum-number-of-calls: 5
        automatic-transition-from-open-to-half-open-enabled: true
        wait-duration-in-open-state: 5s
        permitted-number-of-calls-in-half-open-state: 3
        sliding-window-size: 10
        sliding-window-type: count_based
    instances:
      CircuitBreakerService:
        base-config: default
```
## Retry
요청 실패 시 재시도 처리 기능을 제공한다.
## TimeLimiter
실행 시간에 대한 제한을 설정할 수 있는 기능을 제공한다.
## Bulkhead
동시에 실행할 수 있는 횟수 제한을 설정할 수 있는 기능을 제공한다.
## RateLimiter
제한치를 넘어 요청을 거부하거나 Queue를 생성하여 처리하는 기능을 제공한다.
## Cache
결과를 캐싱하는 기능을 제공한다.

# Ref
- https://mangkyu.tistory.com/290#:~:text=%5B%20Resilience4J%EB%9E%80%3F%20%5D,%ED%8C%A8%ED%84%B4%EC%9D%84%20%EC%9C%84%ED%95%B4%20%EC%82%AC%EC%9A%A9%EB%90%9C%EB%8B%A4.
- https://velog.io/@akfls221/resilience4j-%EB%A1%9C-%EC%95%8C%EC%95%84%EB%B3%B4%EB%8A%94-%EC%84%9C%ED%82%B7%EB%B8%8C%EB%A0%88%EC%9D%B4%EC%BB%A4%ED%8C%A8%ED%84%B4CircuitBreaker
- https://oliveyoung.tech/blog/2023-08-31/circuitbreaker-inventory-squad/
