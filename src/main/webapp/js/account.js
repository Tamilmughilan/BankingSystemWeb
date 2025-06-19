
   function updateStorageType() {
       const selectedStorage = document.querySelector('input[name="storageType"]:checked').value;
       const forms = document.querySelectorAll('form');
       
       forms.forEach(form => {
           const storageInput = form.querySelector('input[name="storageType"]');
           if (storageInput) {
               storageInput.value = selectedStorage;
           }
       });
   }

   document.addEventListener('DOMContentLoaded', function() {
       const storageRadios = document.querySelectorAll('input[name="storageType"]');
       storageRadios.forEach(radio => {
           radio.addEventListener('change', updateStorageType);
       });
   });

  
       function showSection(sectionId) {

           const sections = document.querySelectorAll('.content-section');
           sections.forEach(section => section.style.display = 'none');
           
          
           document.getElementById(sectionId).style.display = 'block';
           
        
           const buttons = document.querySelectorAll('.operation-btn');
           buttons.forEach(btn => btn.classList.remove('active'));
           event.target.classList.add('active');
       }

  
       function showTab(tabId) {
           const tabs = document.querySelectorAll('#transactions .tab-content');
           tabs.forEach(tab => tab.classList.remove('active'));
           
           const buttons = document.querySelectorAll('#transactions .tab-btn');
           buttons.forEach(btn => btn.classList.remove('active'));
           
           document.getElementById(tabId).classList.add('active');
           event.target.classList.add('active');
       }

       
       function showViewTab(tabId) {
           const tabs = document.querySelectorAll('#view-accounts .tab-content');
           tabs.forEach(tab => tab.classList.remove('active'));
           
           const buttons = document.querySelectorAll('#view-accounts .tab-btn');
           buttons.forEach(btn => btn.classList.remove('active'));
           
           document.getElementById(tabId).classList.add('active');
           event.target.classList.add('active');
       }

 
       function showJointTab(tabId) {
           const tabs = document.querySelectorAll('#joint-accounts .tab-content');
           tabs.forEach(tab => tab.classList.remove('active'));
           
           const buttons = document.querySelectorAll('#joint-accounts .tab-btn');
           buttons.forEach(btn => btn.classList.remove('active'));
           
           document.getElementById(tabId).classList.add('active');
           event.target.classList.add('active');
       }
   
   $(document).ready(function() {
       var csrfToken = '<%= session.getAttribute("csrfToken") %>';
       
       function clearMessages() {
           $('#result-message').empty();
           $('#account-details').empty();
           $('#account-list').empty();
           $('#transaction-result').empty();
       }
       
       function showLoading(message) {
           return '<div class="info-message"><h3>' + message + '</h3></div>';
       }

       //Create Account
       $('#createAccountForm').on('submit', function(e) {
           e.preventDefault();
           clearMessages();
           $('#result-message').html(showLoading('Creating account...'));
           
           var selectedStorage = $('input[name="storageType"]:checked').val();
           var formData = $(this).serialize().replace(/storageType=[^&]*/, 'storageType=' + selectedStorage);
           if (formData.indexOf('csrfToken') === -1) {
               formData += '&csrfToken=' + encodeURIComponent(csrfToken);
           }
           
           $.ajax({
               url: 'account',
               type: 'POST',
               data: formData,
               headers: { 'X-Requested-With': 'XMLHttpRequest' },
               success: function(response) {
                   $('#result-message').empty();
                   if (response.success) {
                       $('#result-message').html(
                           '<div class="success-message">' +
                           '<h2>' + response.message + '</h2>' + 
                           (response.data ? '<p><strong>New Account Number:</strong> ' + response.data + '</p>' : '') + 
                           '</div>'
                       );
                       $('#createAccountForm')[0].reset();
                   } else {
                       $('#result-message').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                   }        
               },
               error: function(xhr, status, error) {
                   $('#result-message').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
               }
           });
       });

       // View Account
       $('#viewAccountForm').on('submit', function(e) {
           e.preventDefault();
           clearMessages();
           $('#account-details').html(showLoading('Loading account details...'));
           
           $.ajax({
               url: 'account',
               type: 'GET',
               data: $(this).serialize(),
               headers: { 'X-Requested-With': 'XMLHttpRequest' },
               success: function(response) {
                   $('#account-details').empty();
                   if (response.success) {
                       $('#account-details').html(
                           '<div class="account-details">' +
                           '<h2>Account Details</h2>' +
                           '<div class="account-info">' + 
                           response.data.replace(/\n/g, '<br>') + 
                           '</div>' +
                           '</div>'
                       );
                   } else {
                       $('#account-details').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                   }
               },
               error: function(xhr, status, error) {
                   $('#account-details').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
               }
           });
       });

       // View Accounts by Branch
       $('#viewAccountsByBranchForm').on('submit', function(e) {
           e.preventDefault();
           clearMessages();
           $('#account-list').html(showLoading('Loading branch accounts...'));
           
           $.ajax({
               url: 'account',
               type: 'GET',
               data: $(this).serialize(),
               headers: { 'X-Requested-With': 'XMLHttpRequest' },
               success: function(response) {
                   $('#account-list').empty();
                   if (response.success) {
                       var accountsHtml = '<div class="accounts-list"><h2>Branch Accounts</h2>';
                       if (Array.isArray(response.data)) {
                           response.data.forEach(function(account) {
                               accountsHtml += '<div class="account-item">' + account.replace(/\n/g, '<br>') + '</div><hr>';
                           });
                       } else {
                           accountsHtml += '<div class="account-item">' + response.data.replace(/\n/g, '<br>') + '</div>';
                       }
                       accountsHtml += '</div>';
                       $('#account-list').html(accountsHtml);
                   } else {
                       $('#account-list').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                   }
               },
               error: function(xhr, status, error) {
                   $('#account-list').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
               }
           });
       });

       // View Accounts by Customer
       $('#viewAccountsByCustomerForm').on('submit', function(e) {
           e.preventDefault();
           clearMessages();
           $('#account-list').html(showLoading('Loading customer accounts...'));
           
           $.ajax({
               url: 'account',
               type: 'GET',
               data: $(this).serialize(),
               headers: { 'X-Requested-With': 'XMLHttpRequest' },
               success: function(response) {
                   $('#account-list').empty();
                   if (response.success) {
                       var accountsHtml = '<div class="accounts-list"><h2>Customer Accounts</h2>';
                       if (Array.isArray(response.data)) {
                           response.data.forEach(function(account) {
                               accountsHtml += '<div class="account-item">' + account.replace(/\n/g, '<br>') + '</div><hr>';
                           });
                       } else {
                           accountsHtml += '<div class="account-item">' + response.data.replace(/\n/g, '<br>') + '</div>';
                       }
                       accountsHtml += '</div>';
                       $('#account-list').html(accountsHtml);
                   } else {
                       $('#account-list').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                   }
               },
               error: function(xhr, status, error) {
                   $('#account-list').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
               }
           });
       });

       // Deposit
       $('#depositForm').on('submit', function(e) {
           e.preventDefault();
           clearMessages();
           $('#transaction-result').html(showLoading('Processing deposit...'));
           
           var formData = $(this).serialize();
           if (formData.indexOf('csrfToken') === -1) {
               formData += '&csrfToken=' + encodeURIComponent(csrfToken);
           }
           
           $.ajax({
               url: 'account',
               type: 'POST',
               data: formData,
               headers: { 'X-Requested-With': 'XMLHttpRequest' },
               success: function(response) {
                   $('#transaction-result').empty();
                   if (response.success) {
                       $('#transaction-result').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                       $('#depositForm')[0].reset();
                   } else {
                       $('#transaction-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                   }
               },
               error: function(xhr, status, error) {
                   $('#transaction-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
               }
           });
       });

       // Withdraw
       $('#withdrawForm').on('submit', function(e) {
           e.preventDefault();
           clearMessages();
           $('#transaction-result').html(showLoading('Processing withdrawal...'));
           
           var formData = $(this).serialize();
           if (formData.indexOf('csrfToken') === -1) {
               formData += '&csrfToken=' + encodeURIComponent(csrfToken);
           }
           
           $.ajax({
               url: 'account',
               type: 'POST',
               data: formData,
               headers: { 'X-Requested-With': 'XMLHttpRequest' },
               success: function(response) {
                   $('#transaction-result').empty();
                   if (response.success) {
                       $('#transaction-result').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                       $('#withdrawForm')[0].reset();
                   } else {
                       $('#transaction-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                   }
               },
               error: function(xhr, status, error) {
                   $('#transaction-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
               }
           });
       });
    
       $('#createJointAccountForm').on('submit', function(e) {
           e.preventDefault();
           clearMessages();
           $('#result-message').html(showLoading('Creating joint account...'));
           
           var formData = $(this).serialize();
           if (formData.indexOf('csrfToken') === -1) {
               formData += '&csrfToken=' + encodeURIComponent(csrfToken);
           }
           
           $.ajax({
               url: 'account',
               type: 'POST',
               data: formData,
               headers: { 'X-Requested-With': 'XMLHttpRequest' },
               success: function(response) {
                   $('#result-message').empty();
                   if (response.success) {
                       $('#result-message').html(
                           '<div class="success-message">' +
                           '<h2>' + response.message + '</h2>' + 
                           (response.data ? '<p><strong>New Account Number:</strong> ' + response.data + '</p>' : '') + 
                           '</div>'
                       );
                       $('#createJointAccountForm')[0].reset();
                   } else {
                       $('#result-message').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                   }
               },
               error: function(xhr, status, error) {
                   $('#result-message').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
               }
           });
       });

       $('#addJointHolderForm').on('submit', function(e) {
           e.preventDefault();
           clearMessages();
           $('#transaction-result').html(showLoading('Adding joint holder...'));
           
           var formData = $(this).serialize();
           if (formData.indexOf('csrfToken') === -1) {
               formData += '&csrfToken=' + encodeURIComponent(csrfToken);
           }
           
           $.ajax({
               url: 'account',
               type: 'POST',
               data: formData,
               headers: { 'X-Requested-With': 'XMLHttpRequest' },
               success: function(response) {
                   $('#transaction-result').empty();
                   if (response.success) {
                       $('#transaction-result').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                       $('#addJointHolderForm')[0].reset();
                   } else {
                       $('#transaction-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                   }
               },
               error: function(xhr, status, error) {
                   $('#transaction-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
               }
           });
       });

       $('#removeJointHolderForm').on('submit', function(e) {
           e.preventDefault();
           clearMessages();
           $('#transaction-result').html(showLoading('Removing joint holder...'));
           
           var formData = $(this).serialize();
           if (formData.indexOf('csrfToken') === -1) {
               formData += '&csrfToken=' + encodeURIComponent(csrfToken);
           }
           
           $.ajax({
               url: 'account',
               type: 'POST',
               data: formData,
               headers: { 'X-Requested-With': 'XMLHttpRequest' },
               success: function(response) {
                   $('#transaction-result').empty();
                   if (response.success) {
                       $('#transaction-result').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                       $('#removeJointHolderForm')[0].reset();
                   } else {
                       $('#transaction-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                   }
               },
               error: function(xhr, status, error) {
                   $('#transaction-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
               }
           });
       });

       $('#viewJointHoldersForm').on('submit', function(e) {
           e.preventDefault();
           clearMessages();
           $('#account-list').html(showLoading('Loading account holders...'));
           
           $.ajax({
               url: 'account',
               type: 'GET',
               data: $(this).serialize(),
               headers: { 'X-Requested-With': 'XMLHttpRequest' },
               success: function(response) {
                   $('#account-list').empty();
                   if (response.success) {
                       var holdersHtml = '<div class="accounts-list"><h2>Account Holders</h2>';
                       if (Array.isArray(response.data)) {
                           holdersHtml += '<ul class="holder-list">';
                           response.data.forEach(function(holderString) {
                               // Parse the string format "{name=sidharth, id=15}"
                               var nameMatch = holderString.match(/name=([^,}]+)/);
                               var idMatch = holderString.match(/id=([^,}]+)/);
                               
                               var name = nameMatch ? nameMatch[1] : 'Unknown';
                               var id = idMatch ? idMatch[1] : 'Unknown';
                               
                               holdersHtml += '<li>' + name + ' (ID: ' + id + ')</li>';
                           });
                           holdersHtml += '</ul>';
                       } else {
                           holdersHtml += '<div class="account-item">' + response.data + '</div>';
                       }
                       holdersHtml += '</div>';
                       $('#account-list').html(holdersHtml);
                   } else {
                       $('#account-list').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                   }
               },
               error: function(xhr, status, error) {
                   $('#account-list').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
               }
           });
       });

       
   });