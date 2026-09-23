$ErrorActionPreference = "Stop"

$OutputDir = $PSScriptRoot
$NewLine = "`r`n"

function Convert-ToXmlText {
    param([string]$Value)
    return [System.Security.SecurityElement]::Escape($Value)
}

function New-UserDefinedVariables {
    $variables = @(
        @("protocol", "&#36;{__P(protocol,http)}"),
        @("host", "&#36;{__P(host,localhost)}"),
        @("port", "&#36;{__P(port,8080)}"),
        @("adminUsername", "admin"),
        @("adminPassword", "admin123"),
        @("activityId", "&#36;{__P(activityId,1)}"),
        @("skuId", "&#36;{__P(skuId,1001)}"),
        @("contentType", "application/json")
    )

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add('      <Arguments guiclass="ArgumentsPanel" testclass="Arguments" testname="TokenMall Variables" enabled="true">')
    $lines.Add('        <collectionProp name="Arguments.arguments">')
    foreach ($variable in $variables) {
        $name = Convert-ToXmlText $variable[0]
        $value = $variable[1]
        $lines.Add('          <elementProp name="' + $name + '" elementType="Argument">')
        $lines.Add('            <stringProp name="Argument.name">' + $name + '</stringProp>')
        $lines.Add('            <stringProp name="Argument.value">' + $value + '</stringProp>')
        $lines.Add('            <stringProp name="Argument.metadata">=</stringProp>')
        $lines.Add('          </elementProp>')
    }
    $lines.Add('        </collectionProp>')
    $lines.Add('      </Arguments>')
    return ($lines -join $NewLine)
}

function New-HttpDefaults {
    return @'
      <ConfigTestElement guiclass="HttpDefaultsGui" testclass="ConfigTestElement" testname="HTTP Request Defaults" enabled="true">
        <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
          <collectionProp name="Arguments.arguments"/>
        </elementProp>
        <stringProp name="HTTPSampler.domain">&#36;{host}</stringProp>
        <stringProp name="HTTPSampler.port">&#36;{port}</stringProp>
        <stringProp name="HTTPSampler.protocol">&#36;{protocol}</stringProp>
        <stringProp name="HTTPSampler.contentEncoding">UTF-8</stringProp>
        <stringProp name="HTTPSampler.connect_timeout">5000</stringProp>
        <stringProp name="HTTPSampler.response_timeout">15000</stringProp>
      </ConfigTestElement>
      <hashTree/>
'@
}

function New-SummaryReport {
    return @'
      <ResultCollector guiclass="SummaryReport" testclass="ResultCollector" testname="Summary Report" enabled="true">
        <boolProp name="ResultCollector.error_logging">false</boolProp>
        <objProp>
          <name>saveConfig</name>
          <value class="SampleSaveConfiguration">
            <time>true</time>
            <latency>true</latency>
            <timestamp>true</timestamp>
            <success>true</success>
            <label>true</label>
            <code>true</code>
            <message>true</message>
            <threadName>true</threadName>
            <dataType>true</dataType>
            <encoding>false</encoding>
            <assertions>true</assertions>
            <subresults>true</subresults>
            <responseData>false</responseData>
            <samplerData>false</samplerData>
            <xml>false</xml>
            <fieldNames>true</fieldNames>
            <responseHeaders>false</responseHeaders>
            <requestHeaders>false</requestHeaders>
            <responseDataOnError>false</responseDataOnError>
            <saveAssertionResultsFailureMessage>true</saveAssertionResultsFailureMessage>
            <assertionsResultsToSave>0</assertionsResultsToSave>
            <bytes>true</bytes>
            <sentBytes>true</sentBytes>
            <url>true</url>
            <threadCounts>true</threadCounts>
            <idleTime>true</idleTime>
            <connectTime>true</connectTime>
          </value>
        </objProp>
        <stringProp name="filename"></stringProp>
      </ResultCollector>
      <hashTree/>
'@
}

function New-HeaderManager {
    param(
        [hashtable]$Headers
    )

    if ($Headers.Count -eq 0) {
        return ''
    }

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add('          <HeaderManager guiclass="HeaderPanel" testclass="HeaderManager" testname="HTTP Header Manager" enabled="true">')
    $lines.Add('            <collectionProp name="HeaderManager.headers">')
    foreach ($name in $Headers.Keys) {
        $lines.Add('              <elementProp name="" elementType="Header">')
        $lines.Add('                <stringProp name="Header.name">' + (Convert-ToXmlText $name) + '</stringProp>')
        $lines.Add('                <stringProp name="Header.value">' + $Headers[$name] + '</stringProp>')
        $lines.Add('              </elementProp>')
    }
    $lines.Add('            </collectionProp>')
    $lines.Add('          </HeaderManager>')
    $lines.Add('          <hashTree/>')
    return ($lines -join $NewLine)
}

function New-ResponseAssertion {
    param(
        [string]$Name,
        [string]$Field,
        [string]$Expected,
        [int]$TestType
    )

    $expectedXml = Convert-ToXmlText $Expected
    return @"
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="$Name" enabled="true">
            <collectionProp name="Asserion.test_strings">
              <stringProp name="49586">$expectedXml</stringProp>
            </collectionProp>
            <stringProp name="Assertion.custom_message">$Name</stringProp>
            <stringProp name="Assertion.test_field">$Field</stringProp>
            <boolProp name="Assertion.assume_success">false</boolProp>
            <intProp name="Assertion.test_type">$TestType</intProp>
          </ResponseAssertion>
          <hashTree/>
"@
}

function New-JsonExtractor {
    param(
        [string]$ReferenceName,
        [string]$JsonPath
    )

    return @"
          <JSONPostProcessor guiclass="JSONPostProcessorGui" testclass="JSONPostProcessor" testname="Extract $ReferenceName" enabled="true">
            <stringProp name="JSONPostProcessor.referenceNames">$ReferenceName</stringProp>
            <stringProp name="JSONPostProcessor.jsonPathExprs">$JsonPath</stringProp>
            <stringProp name="JSONPostProcessor.match_numbers">1</stringProp>
            <stringProp name="JSONPostProcessor.defaultValues">NOT_FOUND</stringProp>
            <stringProp name="JSONPostProcessor.compute_concat">false</stringProp>
            <stringProp name="JSONPostProcessor.scope">main</stringProp>
          </JSONPostProcessor>
          <hashTree/>
"@
}

function New-Jsr223PreProcessor {
    param(
        [string]$Name,
        [string]$Script
    )

    return @"
          <JSR223PreProcessor guiclass="TestBeanGUI" testclass="JSR223PreProcessor" testname="$Name" enabled="true">
            <stringProp name="scriptLanguage">groovy</stringProp>
            <stringProp name="parameters"></stringProp>
            <stringProp name="filename"></stringProp>
            <stringProp name="cacheKey">true</stringProp>
            <stringProp name="script">$(Convert-ToXmlText $Script)</stringProp>
          </JSR223PreProcessor>
          <hashTree/>
"@
}

function New-HttpSampler {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Path,
        [string]$Body = "",
        [hashtable]$Headers = @{},
        [bool]$AssertHttp200 = $true,
        [bool]$AssertCodeZero = $true,
        [bool]$IgnoreStatus = $false,
        [string]$ExtractName = "",
        [string]$ExtractPath = "",
        [string]$PreProcessorName = "",
        [string]$PreProcessorScript = ""
    )

    $pathXml = Convert-ToXmlText $Path
    $bodyXml = Convert-ToXmlText $Body
    $rawBody = $Method -eq "POST" -and $Body.Length -gt 0

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add('        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="' + (Convert-ToXmlText $Name) + '" enabled="true">')
    $lines.Add('          <boolProp name="HTTPSampler.postBodyRaw">' + $rawBody.ToString().ToLowerInvariant() + '</boolProp>')
    $lines.Add('          <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">')
    $lines.Add('            <collectionProp name="Arguments.arguments">')
    if ($rawBody) {
        $lines.Add('              <elementProp name="" elementType="HTTPArgument">')
        $lines.Add('                <boolProp name="HTTPArgument.always_encode">false</boolProp>')
        $lines.Add('                <stringProp name="Argument.value">' + $bodyXml + '</stringProp>')
        $lines.Add('                <stringProp name="Argument.metadata">=</stringProp>')
        $lines.Add('              </elementProp>')
    }
    $lines.Add('            </collectionProp>')
    $lines.Add('          </elementProp>')
    $lines.Add('          <stringProp name="HTTPSampler.domain"></stringProp>')
    $lines.Add('          <stringProp name="HTTPSampler.port"></stringProp>')
    $lines.Add('          <stringProp name="HTTPSampler.protocol"></stringProp>')
    $lines.Add('          <stringProp name="HTTPSampler.contentEncoding">UTF-8</stringProp>')
    $lines.Add('          <stringProp name="HTTPSampler.path">' + $pathXml + '</stringProp>')
    $lines.Add('          <stringProp name="HTTPSampler.method">' + $Method + '</stringProp>')
    $lines.Add('          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>')
    $lines.Add('          <boolProp name="HTTPSampler.auto_redirects">false</boolProp>')
    $lines.Add('          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>')
    $lines.Add('          <boolProp name="HTTPSampler.DO_MULTIPART_POST">false</boolProp>')
    $lines.Add('          <stringProp name="HTTPSampler.embedded_url_re"></stringProp>')
    $lines.Add('          <stringProp name="HTTPSampler.connect_timeout">5000</stringProp>')
    $lines.Add('          <stringProp name="HTTPSampler.response_timeout">15000</stringProp>')
    if ($IgnoreStatus) {
        $lines.Add('          <boolProp name="HTTPSampler.ignoreStatus">true</boolProp>')
    }
    $lines.Add('        </HTTPSamplerProxy>')
    $lines.Add('        <hashTree>')

    if ($PreProcessorName.Length -gt 0) {
        $lines.Add((New-Jsr223PreProcessor -Name $PreProcessorName -Script $PreProcessorScript))
    }
    $headerManager = New-HeaderManager -Headers $Headers
    if ($headerManager.Length -gt 0) {
        $lines.Add($headerManager)
    }
    if ($AssertHttp200 -and -not $IgnoreStatus) {
        $lines.Add((New-ResponseAssertion -Name "HTTP 200" -Field "Assertion.response_code" -Expected "200" -TestType 8))
    }
    if ($AssertCodeZero -and -not $IgnoreStatus) {
        $lines.Add((New-ResponseAssertion -Name "API code 0" -Field "Assertion.response_data" -Expected '"code":0' -TestType 2))
    }
    if ($ExtractName.Length -gt 0 -and $ExtractPath.Length -gt 0) {
        $lines.Add((New-JsonExtractor -ReferenceName $ExtractName -JsonPath $ExtractPath))
    }

    $lines.Add('        </hashTree>')
    return ($lines -join $NewLine)
}

function New-ThreadGroup {
    param(
        [string]$Name,
        [string]$Threads,
        [string]$RampSeconds,
        [string]$Loops,
        [string]$Content
    )

    return @"
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="$Name" enabled="true">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">$Loops</stringProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">$Threads</stringProp>
        <stringProp name="ThreadGroup.ramp_time">$RampSeconds</stringProp>
        <boolProp name="ThreadGroup.scheduler">false</boolProp>
        <stringProp name="ThreadGroup.duration"></stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
        <boolProp name="ThreadGroup.same_user_on_next_iteration">true</boolProp>
      </ThreadGroup>
      <hashTree>
$Content
      </hashTree>
"@
}

function New-TestPlan {
    param(
        [string]$Name,
        [string]$Comment,
        [string]$ThreadGroups
    )

    return @"
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.6.3">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="$Name" enabled="true">
      <stringProp name="TestPlan.comments">$(Convert-ToXmlText $Comment)</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.tearDown_on_shutdown">true</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
    </TestPlan>
    <hashTree>
$(New-UserDefinedVariables)
      <hashTree/>
$(New-HttpDefaults)
$(New-SummaryReport)
$ThreadGroups
    </hashTree>
  </hashTree>
</jmeterTestPlan>
"@
}

function Write-Plan {
    param(
        [string]$FileName,
        [string]$Content
    )

    $path = Join-Path $OutputDir $FileName
    [System.IO.File]::WriteAllText($path, $Content, [System.Text.UTF8Encoding]::new($false))
    Write-Host "Created $path"
}

$adminAuth = @{ Authorization = "Bearer &#36;{accessToken}" }
$jsonHeader = @{ "Content-Type" = "application/json" }
$jsonAuthHeader = @{
    "Content-Type" = "application/json"
    Authorization = "Bearer &#36;{accessToken}"
}

$smokeContent = @(
    (New-HttpSampler -Name "GET categories" -Method "GET" -Path "/api/v1/categories"),
    (New-HttpSampler -Name "GET token packs" -Method "GET" -Path "/api/v1/products?type=TOKEN_PACK&page=1&size=20"),
    (New-HttpSampler -Name "GET product detail" -Method "GET" -Path "/api/v1/products/101"),
    (New-HttpSampler -Name "POST login admin" -Method "POST" -Path "/api/v1/auth/login" -Body '{"username":"admin","password":"admin123"}' -Headers $jsonHeader -ExtractName "accessToken" -ExtractPath '$.data.accessToken'),
    (New-HttpSampler -Name "GET auth me" -Method "GET" -Path "/api/v1/auth/me" -Headers $adminAuth),
    (New-HttpSampler -Name "GET cart" -Method "GET" -Path "/api/v1/cart" -Headers $adminAuth),
    (New-HttpSampler -Name "GET token account" -Method "GET" -Path "/api/v1/token/account" -Headers $adminAuth),
    (New-HttpSampler -Name "GET orders" -Method "GET" -Path "/api/v1/orders?page=1&size=10" -Headers $adminAuth),
    (New-HttpSampler -Name "GET seckill activities" -Method "GET" -Path "/api/v1/seckill/activities")
) -join $NewLine

$browseContent = @(
    (New-HttpSampler -Name "GET categories" -Method "GET" -Path "/api/v1/categories"),
    (New-HttpSampler -Name "GET token packs" -Method "GET" -Path "/api/v1/products?type=TOKEN_PACK&page=1&size=20"),
    (New-HttpSampler -Name "GET token plans" -Method "GET" -Path "/api/v1/products?type=TOKEN_PLAN&page=1&size=20"),
    (New-HttpSampler -Name "GET random product detail" -Method "GET" -Path '/api/v1/products/${__Random(101,106)}'),
    (New-HttpSampler -Name "GET seckill activities" -Method "GET" -Path "/api/v1/seckill/activities")
) -join $NewLine

$loginContent = @(
    (New-HttpSampler -Name "POST login admin" -Method "POST" -Path "/api/v1/auth/login" -Body '{"username":"${adminUsername}","password":"${adminPassword}"}' -Headers $jsonHeader -ExtractName "accessToken" -ExtractPath '$.data.accessToken'),
    (New-HttpSampler -Name "GET auth me" -Method "GET" -Path "/api/v1/auth/me" -Headers $adminAuth)
) -join $NewLine

$seckillPreScript = 'def suffix = System.currentTimeMillis().toString() + "_" + ctx.getThreadNum().toString(); vars.put("loadUser", "tokenmall_load_" + suffix); vars.put("loadPassword", "LoadPass123!");'
$seckillContent = @(
    (New-HttpSampler -Name "POST register load user" -Method "POST" -Path "/api/v1/auth/register" -Body '{"username":"${loadUser}","password":"${loadPassword}","nickname":"Load User"}' -Headers $jsonHeader -AssertHttp200 $false -AssertCodeZero $false -IgnoreStatus $true -PreProcessorName "Generate load user" -PreProcessorScript $seckillPreScript),
    (New-HttpSampler -Name "POST login load user" -Method "POST" -Path "/api/v1/auth/login" -Body '{"username":"${loadUser}","password":"${loadPassword}"}' -Headers $jsonHeader -ExtractName "accessToken" -ExtractPath '$.data.accessToken'),
    (New-HttpSampler -Name "GET seckill activity detail" -Method "GET" -Path '/api/v1/seckill/activities/${activityId}'),
    (New-HttpSampler -Name "POST seckill order" -Method "POST" -Path '/api/v1/seckill/activities/${activityId}/orders' -Body '{"requestId":"${__UUID()}","quantity":1}' -Headers $jsonAuthHeader)
) -join $NewLine

$orderContent = @(
    (New-HttpSampler -Name "POST login admin" -Method "POST" -Path "/api/v1/auth/login" -Body '{"username":"${adminUsername}","password":"${adminPassword}"}' -Headers $jsonHeader -ExtractName "accessToken" -ExtractPath '$.data.accessToken'),
    (New-HttpSampler -Name "POST direct order" -Method "POST" -Path "/api/v1/orders/direct" -Body '{"skuId":${skuId},"quantity":1,"requestId":"order-${__UUID()}"}' -Headers $jsonAuthHeader -ExtractName "orderNo" -ExtractPath '$.data.orderNo'),
    (New-HttpSampler -Name "POST mock payment" -Method "POST" -Path "/api/v1/payments/mock/success" -Body '{"orderNo":"${orderNo}","requestId":"pay-${__UUID()}"}' -Headers $jsonAuthHeader)
) -join $NewLine

$plans = @(
    @{
        File = "00-Smoke.jmx"
        Name = "TokenMall Smoke"
        Comment = "1 user, 1 loop. Validates public reads, login, JWT extraction, cart, account, orders, and seckill list."
        Content = (New-ThreadGroup -Name "Smoke" -Threads "1" -RampSeconds "1" -Loops "1" -Content $smokeContent)
    },
    @{
        File = "10-Browse-Read.jmx"
        Name = "TokenMall Browse Read Load"
        Comment = "Read-heavy catalog traffic. Default 30 users, 10s ramp, 10 loops. Override with -Jthreads, -Jramp, -Jloops."
        Content = (New-ThreadGroup -Name "Browse read load" -Threads "&#36;{__P(threads,30)}" -RampSeconds "&#36;{__P(ramp,10)}" -Loops "&#36;{__P(loops,10)}" -Content $browseContent)
    },
    @{
        File = "20-Login.jmx"
        Name = "TokenMall Login Load"
        Comment = "JWT login and auth/me traffic. Default 20 users, 10s ramp, 5 loops. Override with -Jthreads, -Jramp, -Jloops."
        Content = (New-ThreadGroup -Name "Login load" -Threads "&#36;{__P(threads,20)}" -RampSeconds "&#36;{__P(ramp,10)}" -Loops "&#36;{__P(loops,5)}" -Content $loginContent)
    },
    @{
        File = "30-Seckill.jmx"
        Name = "TokenMall Seckill Concurrency"
        Comment = "Registers unique users, logs in, and calls seckill activity. Default 50 users, 5s ramp, 1 loop. Override with -Jthreads, -Jramp, -JactivityId."
        Content = (New-ThreadGroup -Name "Seckill concurrency" -Threads "&#36;{__P(threads,50)}" -RampSeconds "&#36;{__P(ramp,5)}" -Loops "1" -Content $seckillContent)
    },
    @{
        File = "40-Order-Functional.jmx"
        Name = "TokenMall Direct Order Functional"
        Comment = "1 user, 1 loop. Creates one direct order and calls mock payment. Run only on a disposable development database."
        Content = (New-ThreadGroup -Name "Direct order functional" -Threads "1" -RampSeconds "1" -Loops "1" -Content $orderContent)
    }
)

foreach ($plan in $plans) {
    $content = New-TestPlan -Name $plan.Name -Comment $plan.Comment -ThreadGroups $plan.Content
    Write-Plan -FileName $plan.File -Content $content
}

Write-Host "TokenMall JMeter plans generated in $OutputDir"
