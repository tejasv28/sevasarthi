# Fix ProviderDashboard.jsx
$file = 'ProviderDashboard.jsx'
$content = Get-Content $file -Raw -Encoding UTF8

# Stats section - hardcoded labels
$content = $content.Replace("'Total Jobs Completed'", "tr('pd_total_jobs_completed')")
$content = $content.Replace("'All time',", "tr('pd_all_time'),")
$content = $content.Replace("sub: 'This week',", "sub: tr('pd_this_week'),")
$content = $content.Replace("'Total Earnings'", "tr('pd_total_earnings')")
$content = $content.Replace("sub: 'Customer rating',", "sub: tr('pd_customer_rating'),")
$content = $content.Replace("sub: 'Jobs finished',", "sub: tr('pd_jobs_finished'),")
$content = $content.Replace("sub: 'Active & Scheduled',", "sub: tr('pd_active_scheduled'),")
$content = $content.Replace(">All Time</span>", ">{tr('pd_all_time')}</span>")

# Jobs tab
$content = $content.Replace(">New Requests</h3>", ">{tr('pd_new_requests')}</h3>")
$content = $content.Replace("{requests.length} Pending", "{requests.length} {tr('pd_pending')}")
$content = $content.Replace(">No new requests.</p>", ">{tr('pd_no_requests')}</p>")
$content = $content.Replace(">You are fully caught up!</p>", ">{tr('pd_caught_up')}</p>")
$content = $content.Replace(">Payout</p>", ">{tr('pd_payout')}</p>")
$content = $content.Replace(">Decline</button>", ">{tr('pd_decline')}</button>")
$content = $content.Replace(">Accept Job</button>", ">{tr('pd_accept_job')}</button>")
$content = $content.Replace(">Active Jobs</h3>", ">{tr('pd_active_jobs')}</h3>")
$content = $content.Replace(">No active jobs right now.</div>", ">{tr('pd_no_active_jobs')}</div>")
$content = $content.Replace(">Start Journey</button>", ">{tr('pd_start_journey')}</button>")
$content = $content.Replace(">Arrived & Start Work</button>", ">{tr('pd_arrived_start')}</button>")
$content = $content.Replace(">Mark Completed</button>", ">{tr('pd_mark_completed')}</button>")

# Services tab
$content = $content.Replace(">My Services</h2>", ">{tr('pd_my_services')}</h2>")
$content = $content.Replace("Manage your service offerings.", "{tr('pd_manage_offerings')}")
$content = $content.Replace(">Add Service</button>", ">{tr('pd_add_service')}</button>")
$content = $content.Replace('No services added yet. Click "Add Service" to get started.', "{tr('pd_no_services_yet')}")
$content = $content.Replace(">Add New Service</h3>", ">{tr('pd_add_new_service')}</h3>")
$content = $content.Replace("?'Active':'Inactive'}", "?tr('pd_active'):tr('pd_inactive')}")
$content = $content.Replace("?'Deactivate':'Activate'}", "?tr('pd_deactivate'):tr('pd_activate')}")

# Tools tab
$content = $content.Replace("'Total Tools'", "tr('pd_total_tools')")
$content = $content.Replace("'Active Rentals'", "tr('pd_active_rentals')")
$content = $content.Replace("'Rental Earnings'", "tr('pd_rental_earnings')")
$content = $content.Replace(">Tool Inventory</h2>", ">{tr('pd_tool_inventory')}</h2>")
$content = $content.Replace("Manage your rental listings and active orders.", "{tr('pd_manage_listings')}")
$content = $content.Replace(">List New Tool</button>", ">{tr('pd_list_new_tool')}</button>")
$content = $content.Replace(">No tools listed yet.</div>", ">{tr('pd_no_tools_yet')}</div>")
$content = $content.Replace(">List Another Tool</h3>", ">{tr('pd_list_another_tool')}</h3>")
$content = $content.Replace(">Active Tool Rentals</h2>", ">{tr('pd_active_tool_rentals')}</h2>")
$content = $content.Replace("Manage current rentals and deliveries.", "{tr('pd_manage_rentals')}")
$content = $content.Replace(">No active rentals.</p>", ">{tr('pd_no_active_rentals')}</p>")
$content = $content.Replace(">Tool</th>", ">{tr('pd_tool_col')}</th>")
$content = $content.Replace(">Customer</th>", ">{tr('pd_customer_col')}</th>")
$content = $content.Replace(">Duration & Payout</th>", ">{tr('pd_duration_payout')}</th>")
$content = $content.Replace(">Status</th>", ">{tr('pd_status_col')}</th>")
$content = $content.Replace(">Action</th>", ">{tr('pd_action_col')}</th>")
$content = $content.Replace(">Mark Delivered</button>", ">{tr('pd_mark_delivered')}</button>")
$content = $content.Replace(">Confirm Return</button>", ">{tr('pd_confirm_return')}</button>")
$content = $content.Replace("?'Active':tool.status==='rented'?'Rented':'Inactive'}", "?tr('pd_active'):tool.status==='rented'?tr('pd_rented'):tr('pd_inactive')}")
$content = $content.Replace("?'Deactivate':'Activate'}", "?tr('pd_deactivate'):tr('pd_activate')}")

# No image label
$content = $content.Replace(">No image</span>", ">{tr('pd_no_image')}</span>")

# / day label
$content = $content.Replace(">/ day</span>", ">{tr('pd_per_day')}</span>")

Set-Content $file -Value $content -Encoding UTF8 -NoNewline
Write-Host "Done - ProviderDashboard.jsx updated"
