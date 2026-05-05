document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('job-form');
    const cancelBtn = document.getElementById('cancel-btn');
    const saveBtn = document.getElementById('save-btn');
    const statusMessage = document.getElementById('status-message');
    const platformBadge = document.getElementById('platform-badge');
    
    // Get current tab info
    chrome.tabs.query({ active: true, currentWindow: true }, function(tabs) {
        const currentTab = tabs[0];
        const url = currentTab.url;
        const title = currentTab.title;
        
        // Auto-detect platform and fill form
        detectPlatformAndFillForm(url, title);
    });

    // Form submission
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        saveJobApplication();
    });

    // Cancel button
    cancelBtn.addEventListener('click', function() {
        window.close();
    });

    function detectPlatformAndFillForm(url, title) {
        let platform = 'OTHER';
        let platformClass = 'platform-other';
        let platformText = 'Other';
        
        if (url.includes('linkedin.com/jobs')) {
            platform = 'LINKEDIN';
            platformClass = 'platform-linkedin';
            platformText = 'LinkedIn';
            extractLinkedInJobInfo();
        } else if (url.includes('naukri.com')) {
            platform = 'NAUKRI';
            platformClass = 'platform-naukri';
            platformText = 'Naukri';
            extractNaukriJobInfo();
        } else if (url.includes('indeed.com')) {
            platform = 'INDEED';
            platformClass = 'platform-indeed';
            platformText = 'Indeed';
            extractIndeedJobInfo();
        }
        
        // Update platform badge and select
        platformBadge.textContent = platformText;
        platformBadge.className = `platform-badge ${platformClass}`;
        document.getElementById('platform').value = platform;
        
        // Set job link
        document.getElementById('jobLink').value = url;
    }

    function extractLinkedInJobInfo() {
        chrome.scripting.executeScript({
            target: { tabId: chrome.tabs.query({active: true, currentWindow: true}, tabs => tabs[0].id) },
            func: function() {
                // Extract company name
                const companyElement = document.querySelector('[data-test-id="entity-lockup-company-name"]') ||
                                    document.querySelector('.company-name') ||
                                    document.querySelector('h1 + div a');
                
                // Extract job title
                const titleElement = document.querySelector('[data-test-id="job-title"]') ||
                                   document.querySelector('h1') ||
                                   document.querySelector('.job-title');
                
                return {
                    company: companyElement ? companyElement.textContent.trim() : '',
                    role: titleElement ? titleElement.textContent.trim() : ''
                };
            }
        }, (results) => {
            if (results && results[0] && results[0].result) {
                const { company, role } = results[0].result;
                if (company) document.getElementById('company').value = company;
                if (role) document.getElementById('role').value = role;
            }
        });
    }

    function extractNaukriJobInfo() {
        chrome.scripting.executeScript({
            target: { tabId: chrome.tabs.query({active: true, currentWindow: true}, tabs => tabs[0].id) },
            func: function() {
                // Extract company name
                const companyElement = document.querySelector('.jd-comp-name') ||
                                    document.querySelector('[data-testid="company-name"]') ||
                                    document.querySelector('a[href*="/company/"]');
                
                // Extract job title
                const titleElement = document.querySelector('.jd-job-title') ||
                                   document.querySelector('[data-testid="job-title"]') ||
                                   document.querySelector('h1');
                
                return {
                    company: companyElement ? companyElement.textContent.trim() : '',
                    role: titleElement ? titleElement.textContent.trim() : ''
                };
            }
        }, (results) => {
            if (results && results[0] && results[0].result) {
                const { company, role } = results[0].result;
                if (company) document.getElementById('company').value = company;
                if (role) document.getElementById('role').value = role;
            }
        });
    }

    function extractIndeedJobInfo() {
        chrome.scripting.executeScript({
            target: { tabId: chrome.tabs.query({active: true, currentWindow: true}, tabs => tabs[0].id) },
            func: function() {
                // Extract company name
                const companyElement = document.querySelector('[data-testid="inlineHeader-companyName"]') ||
                                    document.querySelector('.companyName') ||
                                    document.querySelector('div[data-testid="jobsearch-CompanyInfo"]');
                
                // Extract job title
                const titleElement = document.querySelector('[data-testid="jobsearch-JobInfoHeader-title"]') ||
                                   document.querySelector('.jobtitle') ||
                                   document.querySelector('h1');
                
                return {
                    company: companyElement ? companyElement.textContent.trim() : '',
                    role: titleElement ? titleElement.textContent.trim() : ''
                };
            }
        }, (results) => {
            if (results && results[0] && results[0].result) {
                const { company, role } = results[0].result;
                if (company) document.getElementById('company').value = company;
                if (role) document.getElementById('role').value = role;
            }
        });
    }

    function saveJobApplication() {
        const formData = new FormData(form);
        const jobData = {
            companyName: formData.get('company'),
            role: formData.get('role'),
            platform: formData.get('platform'),
            jobLink: formData.get('jobLink'),
            notes: formData.get('notes'),
            status: 'APPLIED',
            appliedDate: new Date().toISOString(),
            source: 'EXTENSION'
        };

        // Show loading state
        saveBtn.disabled = true;
        saveBtn.textContent = 'Saving...';
        hideStatusMessage();

        // Send to backend
        fetch('http://localhost:8080/api/applications/extension', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(jobData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to save job application');
            }
            return response.json();
        })
        .then(data => {
            showStatusMessage('Job application saved successfully!', 'success');
            setTimeout(() => {
                window.close();
            }, 1500);
        })
        .catch(error => {
            console.error('Error saving job application:', error);
            showStatusMessage('Failed to save job application. Please try again.', 'error');
        })
        .finally(() => {
            saveBtn.disabled = false;
            saveBtn.textContent = 'Save Job';
        });
    }

    function showStatusMessage(message, type) {
        statusMessage.textContent = message;
        statusMessage.className = `status status-${type}`;
        statusMessage.classList.remove('hidden');
    }

    function hideStatusMessage() {
        statusMessage.classList.add('hidden');
    }
});
