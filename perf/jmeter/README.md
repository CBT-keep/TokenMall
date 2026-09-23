# TokenMall JMeter Load Tests

JMeter 5.6.3 is installed at `D:\JMeter\apache-jmeter-5.6.3`.

The plans target `http://localhost:8080` by default. Start MySQL, RabbitMQ, Redis, the backend, and the frontend before running them.

## Plans

| File | Purpose | Default load |
| --- | --- | --- |
| `00-Smoke.jmx` | Login, JWT, public reads, cart, account, orders, seckill list | 1 user, 1 loop |
| `10-Browse-Read.jmx` | Product and category read traffic | 30 users, 10s ramp, 10 loops |
| `20-Login.jmx` | Login and authenticated user lookup | 20 users, 10s ramp, 5 loops |
| `30-Seckill.jmx` | Unique-user seckill concurrency | 50 users, 5s ramp, 1 loop |
| `40-Order-Functional.jmx` | One direct order plus mock payment | 1 user, 1 loop |

The plans use `admin / admin123` for authenticated smoke and order cases. The seckill plan creates unique development users at runtime.

Regenerate all plans after changing test data or endpoints:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\perf\jmeter\New-TokenMallPlans.ps1
```

## Open In GUI

```powershell
& 'D:\JMeter\apache-jmeter-5.6.3\bin\jmeter.bat'
```

Then open a `.jmx` file from this directory.

GUI usage:

1. Open a plan such as `00-Smoke.jmx`.
2. Use the `Summary Report` listener to see throughput, average response time, and error rate.
3. Click the green Start button or press `Ctrl+R` to run.
4. Use the toolbar Stop button or `Ctrl+.` to stop a running test.
5. Change thread counts, ramp-up, and loop counts in the left-side Thread Group before running larger loads.

## Run From CLI

Smoke test:

```powershell
& 'D:\JMeter\apache-jmeter-5.6.3\bin\jmeter.bat' -n -t .\perf\jmeter\00-Smoke.jmx -l .\perf\jmeter\results\smoke.jtl -e -o .\perf\jmeter\results\smoke-report
```

Browse load with overrides:

```powershell
& 'D:\JMeter\apache-jmeter-5.6.3\bin\jmeter.bat' -n -t .\perf\jmeter\10-Browse-Read.jmx -Jthreads=50 -Jramp=15 -Jloops=20 -l .\perf\jmeter\results\browse.jtl -e -o .\perf\jmeter\results\browse-report
```

Seckill concurrency:

```powershell
& 'D:\JMeter\apache-jmeter-5.6.3\bin\jmeter.bat' -n -t .\perf\jmeter\30-Seckill.jmx -Jthreads=100 -Jramp=10 -JactivityId=1 -l .\perf\jmeter\results\seckill.jtl -e -o .\perf\jmeter\results\seckill-report
```

The `-e -o` report directory must not already exist.
