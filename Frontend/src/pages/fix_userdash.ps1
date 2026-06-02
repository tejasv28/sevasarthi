# Fix UserDashboard.jsx
$file = 'UserDashboard.jsx'
$content = Get-Content $file -Raw -Encoding UTF8

# Header subtitle
$content = $content.Replace('Manage your services and track progress.', "{t('ud_manage_services')}")

# Header buttons
$content = $content.Replace('>Raise Complaint', ">{t('ud_raise_complaint')}")
$content = $content.Replace('>My Complaints', ">{t('ud_my_complaints')}")
$content = $content.Replace('>Book Service', ">{t('nav_book_service')}")

# Total label
$content = $content.Replace('">Total</span>', ">{t('book_total')}</span>")

# Job Progress label
$content = $content.Replace('>Job Progress</h5>', ">{t('ud_job_progress')}</h5>")

# Start/completion code helper text
$content = $content.Replace('Share this with provider to begin work', "{t('ud_share_start_code')}")
$content = $content.Replace('Share this when work is finished', "{t('ud_share_completion_code')}")

# Provider unresponsive warning + reassign
$content = $content.Replace('Provider unresponsive for 15 mins', "{t('ud_provider_unresponsive')}")
$content = $content.Replace('>Reassign Vendor', ">{t('ud_reassign_vendor')}")

# On track label
$content = $content.Replace('> On track', ">{t('ud_on_track')}")

# Call / WhatsApp buttons
$content = $content.Replace('> Call', ">{t('ud_call')}")

# Table headers
$content = $content.Replace('">Service Details</th>', ">{t('ud_service_details')}</th>")
$content = $content.Replace('">Date</th>', ">{t('ud_date')}</th>")
$content = $content.Replace('">Amount</th>', ">{t('ud_amount')}</th>")
$content = $content.Replace('">Action</th>', ">{t('ud_action')}</th>")
$content = $content.Replace('No past services found.', "{t('ud_no_past_services')}")

# Past Tool Rentals heading + table headers
$content = $content.Replace('>Past Tool Rentals</h3>', ">{t('ud_past_tool_rentals')}</h3>")
$content = $content.Replace('">Tool Details</th>', ">{t('ud_tool_details')}</th>")
$content = $content.Replace('">Duration</th>', ">{t('ud_duration')}</th>")
$content = $content.Replace('">Status</th>', ">{t('ud_status_col')}</th>")
$content = $content.Replace('> Days', ">{t('ud_days')}")
$content = $content.Replace('"Provider: {r.toolId', '"' + "{t('book_provider')}: {r.toolId")

# Delivery / Return OTP labels
$content = $content.Replace('>Delivery OTP</p>', ">{t('ud_delivery_otp')}</p>")
$content = $content.Replace('>Return OTP</p>', ">{t('ud_return_otp')}</p>")

# Manage Orders button
$content = $content.Replace('Manage Orders', "{t('ud_manage_orders')}")

# Recommended section
$content = $content.Replace('>Recommended for you</h3>', ">{t('ud_recommended')}</h3>")
$content = $content.Replace('Discover more services tailored for your home.', "{t('ud_recommended_desc')}")
$content = $content.Replace('Browse Services ', "{t('ud_browse_services')} ")

# Feedback modal
$content = $content.Replace('>Rate Service</h3>', ">{t('ud_rate_service')}</h3>")
$content = $content.Replace('>Job Details</p>', ">{t('ud_job_details')}</p>")
$content = $content.Replace('How was your experience?', "{t('ud_how_was_experience')}")
$content = $content.Replace('>Leave a Comment (Optional)</label>', ">{t('ud_leave_comment')}</label>")
$content = $content.Replace('Tell us what you liked or what could be improved...', "{t('ud_comment_placeholder')}")
$content = $content.Replace('>Submit Feedback', ">{t('ud_submit_feedback')}")

Set-Content $file -Value $content -Encoding UTF8 -NoNewline
Write-Host "Done - UserDashboard.jsx updated"
