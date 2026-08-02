param(
    [string]$BaseUrl = "http://localhost:8081/api",
    [string]$Email = "trader@db.com",
    [string]$Password = "trader123",
    [int]$TotalRequests = 100,
    [int]$Concurrency = 10
)

$ErrorActionPreference = 'Stop'

$loginBody = @{ email = $Email; password = $Password } | ConvertTo-Json -Compress
$loginResponse = Invoke-RestMethod -Method Post -Uri "$BaseUrl/auth/login" -ContentType 'application/json' -Body $loginBody
$token = $loginResponse.accessToken

if ([string]::IsNullOrWhiteSpace($token)) {
    throw "Login did not return an access token."
}

$requestsPerWorker = [Math]::Floor($TotalRequests / $Concurrency)
$remainder = $TotalRequests % $Concurrency
$jobs = @()

for ($i = 1; $i -le $Concurrency; $i++) {
    $batchSize = $requestsPerWorker
    if ($i -le $remainder) { $batchSize++ }
    $startIndex = ($i - 1) * $requestsPerWorker
    if ($i -le $remainder) { $startIndex = $startIndex + 1 - 1 }

    $jobs += Start-Job -ScriptBlock {
        param($BaseUrl, $Token, $StartIndex, $BatchSize, $JobIndex)

        $client = [System.Net.Http.HttpClient]::new()
        $client.DefaultRequestHeaders.Authorization = [System.Net.Http.Headers.AuthenticationHeaderValue]::new('Bearer', $Token)
        $client.DefaultRequestHeaders.Accept.Add([System.Net.Http.Headers.MediaTypeWithQualityHeaderValue]::new('application/json'))

        $success = 0
        $failures = 0

        for ($n = 0; $n -lt $BatchSize; $n++) {
            $reqNumber = $StartIndex + $n + 1
            $tradeRef = "ADV-{0:D4}" -f $reqNumber
            $payload = @{
                tradeRef = $tradeRef
                instrumentId = 1
                counterpartyId = 1
                assetClass = 'EQUITY'
                side = 'BUY'
                quantity = 100
                price = 245.5
                tradeDate = '2026-06-02'
            } | ConvertTo-Json -Compress

            try {
                $response = $client.PostAsync("$BaseUrl/v1/trades", [System.Net.Http.StringContent]::new($payload, [System.Text.Encoding]::UTF8, 'application/json')).GetAwaiter().GetResult()
                if ($response.IsSuccessStatusCode) { $success++ } else { $failures++ }
            } catch {
                $failures++
            }
        }

        [pscustomobject]@{ Success = $success; Failures = $failures; JobIndex = $JobIndex }
    } -ArgumentList $BaseUrl, $token, $startIndex, $batchSize, $i
}

$jobs | Wait-Job | Out-Null
$summary = @()
$successCount = 0
$failureCount = 0
foreach ($job in $jobs) {
    $result = Receive-Job -Job $job
    $summary += $result
    $successCount += $result.Success
    $failureCount += $result.Failures
    Remove-Job -Job $job
}

Write-Host "Total requests attempted: $($successCount + $failureCount)"
Write-Host "Successful: $successCount"
Write-Host "Failures: $failureCount"

if ($failureCount -gt 0) {
    throw "One or more requests failed during the ADV097 load test."
}

$metricsPage = Invoke-WebRequest -Uri "$BaseUrl/actuator/prometheus" -UseBasicParsing
$metricsText = $metricsPage.Content
$counterLine = ($metricsText -split "`n" | Where-Object { $_ -match 'trade_created_total' } | Select-Object -First 1)
if ($counterLine) {
    Write-Host "Prometheus sample: $counterLine"
} else {
    Write-Host 'Prometheus sample not found for trade_created_total.'
}
