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

application.yml을 통한 서킷 브레이커 예제
```
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
## TimeLimiter
## Bulkhead
## RateLimiter

# Ref
- https://mangkyu.tistory.com/290#:~:text=%5B%20Resilience4J%EB%9E%80%3F%20%5D,%ED%8C%A8%ED%84%B4%EC%9D%84%20%EC%9C%84%ED%95%B4%20%EC%82%AC%EC%9A%A9%EB%90%9C%EB%8B%A4.
- https://velog.io/@akfls221/resilience4j-%EB%A1%9C-%EC%95%8C%EC%95%84%EB%B3%B4%EB%8A%94-%EC%84%9C%ED%82%B7%EB%B8%8C%EB%A0%88%EC%9D%B4%EC%BB%A4%ED%8C%A8%ED%84%B4CircuitBreaker
