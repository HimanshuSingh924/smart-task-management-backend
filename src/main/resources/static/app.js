/**
 * ============================================================
 * TaskFlow Application JavaScript
 * Core architecture: Pure Vanilla JS + Fetch API
 * Super easy to explain to an evaluator!
 * ============================================================
 */

// ── Configuration ──
// Target URL where the Spring Boot REST API is running
const API_BASE_URL = 'http://localhost:8080/api/v1';

// ── Session Helpers ──
// These methods load, save, and clear user session data in the browser's localStorage
const getSession = () => JSON.parse(localStorage.getItem('user_session'));
const saveSession = (userData) => localStorage.setItem('user_session', JSON.stringify(userData));
const clearSession = () => localStorage.removeItem('user_session');

// ── Routing Switcher View Controller ──
// Switches between Login form, Registration form, and the main Task Dashboard
function switchView(viewId) {
  // Hide all screens
  document.getElementById('login-view').style.display = 'none';
  document.getElementById('register-view').style.display = 'none';
  document.getElementById('dashboard-view').style.display = 'none';
  
  // Show the selected screen
  document.getElementById(viewId).style.display = 'block';
}

// ── Toast Notifications ──
// Displays a sleek glowing alert banner at the bottom-right corner for success/error feedback
function triggerToast(message, isError = false) {
  const toastEl = document.getElementById('toast');
  toastEl.textContent = message;
  
  // Change background color based on status
  toastEl.style.backgroundColor = isError ? 'var(--danger)' : 'var(--success)';
  toastEl.style.display = 'block';
  
  // Auto-hide alert after 3 seconds
  setTimeout(() => {
    toastEl.style.display = 'none';
  }, 3000);
}

// ── Application Bootstrapper ──
// Executes automatically as soon as the page is finished loading
window.addEventListener('DOMContentLoaded', () => {
  // Set default due-date in the input form to today
  const todayDate = new Date().toISOString().split('T')[0];
  document.getElementById('task-due').value = todayDate;

  // Retrieve active session token from local storage
  const activeSession = getSession();
  if (activeSession && activeSession.token) {
    showDashboardView();
  } else {
    switchView('login-view');
  }
});

// ── Operation 1: User Registration ──
async function handleRegister() {
  const username = document.getElementById('reg-username').value;
  const email = document.getElementById('reg-email').value;
  const password = document.getElementById('reg-password').value;

  // Form input validation check
  if (!username || !email || !password) {
    return triggerToast('All registration fields are required!', true);
  }

  try {
    // Send register payload to Spring Boot REST API
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password })
    });
    const result = await response.json();

    if (!response.ok) {
      throw new Error(result.message || 'Registration failed.');
    }

    triggerToast('Account successfully created! Logging in...');
    
    // Save token and email inside user session local storage
    saveSession(result.data);
    showDashboardView();
  } catch (error) {
    triggerToast(error.message, true);
  }
}

// ── Operation 2: User Login ──
async function handleLogin() {
  const email = document.getElementById('login-email').value;
  const password = document.getElementById('login-password').value;

  // Form validation check
  if (!email || !password) {
    return triggerToast('Please enter both email and password!', true);
  }

  try {
    // Send credentials payload to Spring Boot REST API
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    const result = await response.json();

    if (!response.ok) {
      throw new Error(result.message || 'Invalid credentials.');
    }

    triggerToast('Welcome back to TaskFlow!');
    
    // Save authentication details in browser storage
    saveSession(result.data);
    showDashboardView();
  } catch (error) {
    triggerToast(error.message, true);
  }
}

// ── Operation 3: Logout Action ──
function handleLogout() {
  clearSession();
  
  // Reset header navbar layout
  document.getElementById('auth-header-info').innerHTML = '';
  switchView('login-view');
  
  // Clear credential forms
  document.getElementById('login-email').value = '';
  document.getElementById('login-password').value = '';
  
  triggerToast('You have signed out.');
}

// ── Operation 4: Load Dashboard ──
function showDashboardView() {
  const session = getSession();
  
  // Inject greeting user indicator and Sign Out button in the header navbar
  document.getElementById('auth-header-info').innerHTML = `
    <div class="navbar-user-tag">Hi, <b>${session.username}</b> 👋</div>
    <button class="navbar-logout-btn" onclick="handleLogout()">Sign Out</button>
  `;
  
  // Switch visible screen to Dashboard
  switchView('dashboard-view');
  
  // Load tasks from MongoDB database
  loadTasksFromDatabase();
}

// ── Operation 5: Fetch Tasks from Server ──
async function loadTasksFromDatabase() {
  const session = getSession();
  const taskListContainer = document.getElementById('task-list-container');
  
  try {
    // Make GET request to Spring Boot REST API with Bearer token authentication
    const response = await fetch(`${API_BASE_URL}/tasks?page=0&size=50`, {
      headers: {
        'Authorization': `Bearer ${session.token}`
      }
    });
    const result = await response.json();

    if (!response.ok) throw new Error('Could not download your tasks.');

    const tasksList = result.data.content;
    
    // Update counter count badge UI
    document.getElementById('task-counter').textContent = `${tasksList.length} Tasks`;

    // Render empty status placeholder if list is empty
    if (tasksList.length === 0) {
      taskListContainer.innerHTML = `
        <div style="text-align: center; color: var(--text-muted); padding: 40px 0; font-size: 14px;">
          📭 Your task feed is empty! Fill out the form above to add your first task.
        </div>`;
      return;
    }

    // Build task card panels dynamically in the DOM
    taskListContainer.innerHTML = tasksList.map(task => `
      <div class="task-card priority-${task.priority}">
        <div class="task-details">
          <h4 class="task-title">${cleanHTML(task.title)}</h4>
          <p class="task-desc">${cleanHTML(task.description || 'No description provided.')}</p>
          <div class="task-meta">
            <!-- Dynamic Status Badge styling matching database value -->
            <span class="badge badge-${task.status}">${task.status.replace('_', ' ')}</span>
            <!-- Priority tag indicator -->
            <span class="badge badge-priority">${task.priority} Priority</span>
            <!-- Optional Due date visual -->
            ${task.dueDate ? `<span class="task-due-tag">📅 Due: ${task.dueDate}</span>` : ''}
          </div>
        </div>
        
        <!-- Live Action triggers -->
        <div class="task-actions">
          <!-- Live Status Dropdown switcher -->
          <select class="status-select" onchange="modifyTaskStatus('${task.id}', this.value)">
            <option value="PENDING" ${task.status === 'PENDING' ? 'selected' : ''}>Pending</option>
            <option value="IN_PROGRESS" ${task.status === 'IN_PROGRESS' ? 'selected' : ''}>In Progress</option>
            <option value="COMPLETED" ${task.status === 'COMPLETED' ? 'selected' : ''}>Completed</option>
          </select>
          <!-- Delete button -->
          <button class="btn btn-danger" onclick="removeTaskFromDatabase('${task.id}')">Delete</button>
        </div>
      </div>
    `).join('');
  } catch (error) {
    triggerToast(error.message, true);
  }
}

// ── Operation 6: Create New Task ──
async function handleCreateTask() {
  const session = getSession();
  const title = document.getElementById('task-title').value;
  const description = document.getElementById('task-desc').value;
  const priority = document.getElementById('task-priority').value;
  const dueDate = document.getElementById('task-due').value;

  // Title validation check
  if (!title) {
    return triggerToast('A task title must be entered!', true);
  }

  try {
    // Send POST payload to Spring Boot REST API
    const response = await fetch(`${API_BASE_URL}/tasks`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${session.token}`
      },
      body: JSON.stringify({ title, description, priority, dueDate, status: 'PENDING' })
    });
    const result = await response.json();

    if (!response.ok) throw new Error(result.message || 'Could not save the task.');

    triggerToast('New task added successfully!');
    
    // Clear title and description inputs in creator form
    document.getElementById('task-title').value = '';
    document.getElementById('task-desc').value = '';
    
    // Reload task feed to show changes
    loadTasksFromDatabase();
  } catch (error) {
    triggerToast(error.message, true);
  }
}

// ── Operation 7: Modify Task Status Dropdown ──
async function modifyTaskStatus(taskId, newStatusValue) {
  const session = getSession();
  try {
    // Send PUT request to status update REST endpoint
    const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${session.token}`
      },
      body: JSON.stringify({ status: newStatusValue })
    });

    if (!response.ok) throw new Error('Could not update task status.');
    
    triggerToast('Task status updated!');
    loadTasksFromDatabase();
  } catch (error) {
    triggerToast(error.message, true);
  }
}

// ── Operation 8: Delete Task ──
async function removeTaskFromDatabase(taskId) {
  if (!confirm('Are you sure you want to delete this task?')) return;
  
  const session = getSession();
  try {
    // Send DELETE request to REST endpoint with path variable
    const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${session.token}`
      }
    });

    if (!response.ok) throw new Error('Could not remove this task.');
    
    triggerToast('Task deleted successfully');
    loadTasksFromDatabase();
  } catch (error) {
    triggerToast(error.message, true);
  }
}

// ── Security Helper: Prevent Cross-Site Scripting (XSS) ──
function cleanHTML(dirtyString) {
  return dirtyString.replace(/[&<>'"]/g, 
    char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[char] || char)
  );
}
