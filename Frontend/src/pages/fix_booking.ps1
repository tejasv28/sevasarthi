$file = 'BookingFlow.jsx'
$content = Get-Content $file -Raw -Encoding UTF8

# No-slots messages
$content = $content -replace 'No slots left for today\.', "{t('book_no_slots')}"
$content = $content -replace 'Please select tomorrow or another date\.', "{t('book_try_tomorrow')}"

# Upload button label  
$content = $content -replace '>Upload<', ">{t('book_upload')}<"

# Coupon applied success
$content = $content -replace 'Coupon applied successfully', "{t('book_coupon_applied')}"

# Available offers heading
$content = $content -replace 'Available Offers for You', "{t('book_available_offers')}"

# Payment option labels
$content = $content -replace '>Online Payment<', ">{t('book_online_payment')}<"
$content = $content -replace '>Pay After Service<', ">{t('book_pay_after_service')}<"

# Sidebar cost breakdown
$content = $content -replace 'Base Rate \(1 hr\)', "{t('book_base_price')}"
$content = $content -replace '>Platform Fee<', ">{t('book_platform_fee')}<"
$content = $content -replace 'Discount \(\{appliedCoupon\.code\}\)', "{t('book_discount')} ({appliedCoupon.code})"
$content = $content -replace [regex]::Escape('Taxes & GST (5%)'), "{t('book_taxes')}"
$content = $content -replace '>Total<', ">{t('book_total')}<"
$content = $content -replace 'Final amount to pay', "{t('book_final_amount')}"

# Trust badge
$old = 'Every booking is protected by the <strong className="text-brand">Seva Guarantee</strong>. Safe and reliable.'
$new = "{t('book_seva_guarantee')}"
$content = $content.Replace($old, $new)

Set-Content $file -Value $content -Encoding UTF8 -NoNewline
Write-Host "Done - BookingFlow.jsx updated"
