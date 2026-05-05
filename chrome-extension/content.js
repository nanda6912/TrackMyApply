// Content script for job pages
// This script runs on LinkedIn, Naukri, and Indeed job pages

(function() {
    'use strict';

    // Add "Save to ApplyTrack" button to job pages
    function addSaveButton() {
        // Check if button already exists
        if (document.getElementById('applytrack-save-btn')) {
            return;
        }

        const button = createSaveButton();
        const targetLocation = findBestButtonLocation();
        
        if (targetLocation) {
            targetLocation.appendChild(button);
        }
    }

    function createSaveButton() {
        const button = document.createElement('button');
        button.id = 'applytrack-save-btn';
        button.textContent = 'Save to ApplyTrack';
        button.style.cssText = `
            background-color: #3b82f6;
            color: white;
            border: none;
            padding: 8px 16px;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            margin: 8px 0;
            transition: background-color 0.2s;
        `;

        button.addEventListener('mouseenter', () => {
            button.style.backgroundColor = '#2563eb';
        });

        button.addEventListener('mouseleave', () => {
            button.style.backgroundColor = '#3b82f6';
        });

        button.addEventListener('click', () => {
            // Open extension popup
            chrome.runtime.sendMessage({ action: 'openPopup' });
        });

        return button;
    }

    function findBestButtonLocation() {
        // LinkedIn
        if (window.location.hostname.includes('linkedin.com')) {
            return document.querySelector('[data-test-id="apply-button"]')?.parentElement ||
                   document.querySelector('.jobs-apply-button')?.parentElement ||
                   document.querySelector('.jobs-apply-button--top-card')?.parentElement;
        }

        // Naukri
        if (window.location.hostname.includes('naukri.com')) {
            return document.querySelector('.jd-apply-btn')?.parentElement ||
                   document.querySelector('[data-testid="apply-btn"]')?.parentElement ||
                   document.querySelector('.apply-btn-container');
        }

        // Indeed
        if (window.location.hostname.includes('indeed.com')) {
            return document.querySelector('[data-testid="apply-button"]')?.parentElement ||
                   document.querySelector('.jobsearch-ApplyButton')?.parentElement ||
                   document.querySelector('.apply-button-container');
        }

        return null;
    }

    // Wait for page to load and then add button
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', addSaveButton);
    } else {
        addSaveButton();
    }

    // Also add button after page changes (for single-page applications)
    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === 'childList') {
                // Check if new content has been added that might contain job information
                const hasJobContent = Array.from(mutation.addedNodes).some(node => {
                    return node.nodeType === Node.ELEMENT_NODE && 
                           (node.querySelector('[data-test-id="job-title"]') ||
                            node.querySelector('.jd-job-title') ||
                            node.querySelector('[data-testid="jobsearch-JobInfoHeader-title"]'));
                });

                if (hasJobContent) {
                    setTimeout(addSaveButton, 1000); // Small delay to ensure DOM is ready
                }
            }
        });
    });

    observer.observe(document.body, {
        childList: true,
        subtree: true
    });

    // Listen for messages from popup
    chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
        if (request.action === 'getJobInfo') {
            const jobInfo = extractJobInfo();
            sendResponse(jobInfo);
        }
        return true;
    });

    function extractJobInfo() {
        const jobInfo = {
            company: '',
            role: '',
            url: window.location.href,
            platform: detectPlatform()
        };

        // LinkedIn
        if (window.location.hostname.includes('linkedin.com')) {
            jobInfo.company = document.querySelector('[data-test-id="entity-lockup-company-name"]')?.textContent?.trim() ||
                             document.querySelector('.company-name')?.textContent?.trim() ||
                             document.querySelector('h1 + div a')?.textContent?.trim() || '';
            
            jobInfo.role = document.querySelector('[data-test-id="job-title"]')?.textContent?.trim() ||
                           document.querySelector('h1')?.textContent?.trim() ||
                           document.querySelector('.job-title')?.textContent?.trim() || '';
        }

        // Naukri
        else if (window.location.hostname.includes('naukri.com')) {
            jobInfo.company = document.querySelector('.jd-comp-name')?.textContent?.trim() ||
                             document.querySelector('[data-testid="company-name"]')?.textContent?.trim() ||
                             document.querySelector('a[href*="/company/"]')?.textContent?.trim() || '';
            
            jobInfo.role = document.querySelector('.jd-job-title')?.textContent?.trim() ||
                           document.querySelector('[data-testid="job-title"]')?.textContent?.trim() ||
                           document.querySelector('h1')?.textContent?.trim() || '';
        }

        // Indeed
        else if (window.location.hostname.includes('indeed.com')) {
            jobInfo.company = document.querySelector('[data-testid="inlineHeader-companyName"]')?.textContent?.trim() ||
                             document.querySelector('.companyName')?.textContent?.trim() ||
                             document.querySelector('div[data-testid="jobsearch-CompanyInfo"]')?.textContent?.trim() || '';
            
            jobInfo.role = document.querySelector('[data-testid="jobsearch-JobInfoHeader-title"]')?.textContent?.trim() ||
                           document.querySelector('.jobtitle')?.textContent?.trim() ||
                           document.querySelector('h1')?.textContent?.trim() || '';
        }

        return jobInfo;
    }

    function detectPlatform() {
        if (window.location.hostname.includes('linkedin.com')) return 'LINKEDIN';
        if (window.location.hostname.includes('naukri.com')) return 'NAUKRI';
        if (window.location.hostname.includes('indeed.com')) return 'INDEED';
        return 'OTHER';
    }
})();
