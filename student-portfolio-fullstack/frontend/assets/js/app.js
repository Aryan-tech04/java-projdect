const API_BASE = "http://localhost:8080/api";
const state = {
  assets: [],
  summary: null,
  selectedAssetId: null,
  captchaAnswer: null,
};

const formatCurrency = (value) =>
  new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(Number(value || 0));

const formatPercent = (value) => `${Number(value || 0).toFixed(2)}%`;

function showToast(message, type = "info") {
  const toast = document.getElementById("toast");
  if (!toast) return;
  toast.textContent = message;
  toast.style.background = type === "error" ? "rgba(127, 29, 29, 0.95)" : "rgba(15, 23, 42, 0.95)";
  toast.classList.add("show");
  setTimeout(() => toast.classList.remove("show"), 2800);
}

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });

  const data = await response.json().catch(() => ({}));
  if (!response.ok || data.success === false) {
    throw new Error(data.message || "Request failed.");
  }
  return data.data;
}

function setUserSession(user) {
  localStorage.setItem("student-ledger-user", JSON.stringify(user));
}

function getUserSession() {
  try {
    return JSON.parse(localStorage.getItem("student-ledger-user") || "null");
  } catch (_) {
    return null;
  }
}

function logout() {
  localStorage.removeItem("student-ledger-user");
  window.location.href = "login.html";
}

function ensureAuthenticated() {
  const user = getUserSession();
  if (!user) {
    window.location.href = "login.html";
    return null;
  }
  const userNameEls = document.querySelectorAll("[data-user-name]");
  userNameEls.forEach((el) => (el.textContent = user.fullName || "Student User"));
  return user;
}

function generateCaptcha() {
  const first = Math.floor(Math.random() * 9) + 1;
  const second = Math.floor(Math.random() * 9) + 1;
  state.captchaAnswer = first + second;
  const captchaEl = document.getElementById("captchaValue");
  if (captchaEl) captchaEl.textContent = `${first} + ${second}`;
}

async function handleLogin(event) {
  event.preventDefault();
  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value.trim();
  const captchaInput = Number(document.getElementById("captchaInput").value.trim());

  if (captchaInput !== state.captchaAnswer) {
    showToast("Captcha answer is incorrect.", "error");
    generateCaptcha();
    return;
  }

  try {
    const user = await request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
    setUserSession(user);
    showToast("Login successful. Redirecting...");
    setTimeout(() => {
      window.location.href = "dashboard.html";
    }, 800);
  } catch (error) {
    showToast(error.message, "error");
  }
}

function attachGlobalActions() {
  document.querySelectorAll("[data-action='logout']").forEach((button) => {
    button.addEventListener("click", logout);
  });
}

function renderSummary(summary) {
  const mapping = {
    totalInvestment: formatCurrency(summary.totalInvestment),
    marketValue: formatCurrency(summary.marketValue),
    totalProfit: formatCurrency(summary.totalProfit),
    averageReturn: formatPercent(summary.averageReturnPercentage),
    totalAssets: summary.totalAssets,
    topCategory: summary.topCategory,
  };

  Object.entries(mapping).forEach(([key, value]) => {
    const element = document.querySelector(`[data-summary='${key}']`);
    if (element) element.textContent = value;
  });

  const profitBadge = document.querySelector("[data-summary='profitBadge']");
  if (profitBadge) {
    const positive = Number(summary.totalProfit) >= 0;
    profitBadge.className = `metric-trend ${positive ? "positive" : "negative"}`;
    profitBadge.textContent = `${positive ? "▲" : "▼"} ${formatPercent(summary.averageReturnPercentage)}`;
  }
}

function categoryClass(category) {
  if (["TECHNOLOGY", "ETF"].includes(category)) return "blue";
  if (["FINANCIAL", "HEALTHCARE"].includes(category)) return "green";
  return "orange";
}

function renderAssetRows(assets, targetId) {
  const tbody = document.getElementById(targetId);
  if (!tbody) return;
  if (!assets.length) {
    tbody.innerHTML = `<tr><td colspan="8"><div class="empty-state">No assets found. Add one from the tracker form.</div></td></tr>`;
    return;
  }

  tbody.innerHTML = assets
    .map((asset) => {
      const gain = ((asset.currentPrice - asset.buyPrice) / asset.buyPrice) * 100;
      return `
        <tr>
          <td><strong>${asset.assetName}</strong><div class="muted small">${asset.notes || "Student managed asset"}</div></td>
          <td>${asset.tickerSymbol}</td>
          <td><span class="badge ${categoryClass(asset.category)}">${asset.category.replace("_", " ")}</span></td>
          <td>${asset.quantity}</td>
          <td>${formatCurrency(asset.buyPrice)}</td>
          <td>${formatCurrency(asset.currentPrice)}</td>
          <td><span class="metric-trend ${gain >= 0 ? "positive" : "negative"}">${gain >= 0 ? "▲" : "▼"} ${formatPercent(gain)}</span></td>
          <td>
            <div class="split">
              <button class="btn-ghost small" onclick="openEditModal(${asset.id})">Edit</button>
              <button class="btn-danger small" onclick="deleteAsset(${asset.id})">Delete</button>
            </div>
          </td>
        </tr>
      `;
    })
    .join("");
}

async function loadDashboard() {
  ensureAuthenticated();
  attachGlobalActions();
  try {
    const [summary, assets] = await Promise.all([request("/dashboard/summary"), request("/assets")]);
    state.summary = summary;
    state.assets = assets;
    renderSummary(summary);
    renderAssetRows(assets.slice(0, 6), "dashboardTableBody");
  } catch (error) {
    showToast(error.message, "error");
  }
}

async function loadTracker() {
  ensureAuthenticated();
  attachGlobalActions();
  bindTrackerEvents();
  await refreshTrackerTable();
}

async function refreshTrackerTable() {
  try {
    const search = document.getElementById("searchInput")?.value?.trim() || "";
    const category = document.getElementById("categoryFilter")?.value || "";
    const query = new URLSearchParams();
    if (search) query.set("search", search);
    if (category) query.set("category", category);
    const queryString = query.toString() ? `?${query.toString()}` : "";
    const [assets, summary] = await Promise.all([request(`/assets${queryString}`), request("/dashboard/summary")]);
    state.assets = assets;
    renderAssetRows(assets, "trackerTableBody");
    renderSummary(summary);
  } catch (error) {
    showToast(error.message, "error");
  }
}

function bindTrackerEvents() {
  document.getElementById("searchInput")?.addEventListener("input", refreshTrackerTable);
  document.getElementById("categoryFilter")?.addEventListener("change", refreshTrackerTable);
  document.getElementById("assetForm")?.addEventListener("submit", saveAsset);
  document.getElementById("openModalBtn")?.addEventListener("click", () => openCreateModal());
  document.getElementById("closeModalBtn")?.addEventListener("click", closeModal);
  document.getElementById("cancelModalBtn")?.addEventListener("click", closeModal);
}

function openCreateModal() {
  state.selectedAssetId = null;
  document.getElementById("modalTitle").textContent = "Add new asset";
  document.getElementById("assetForm").reset();
  document.getElementById("assetModal").classList.add("show");
}

function closeModal() {
  document.getElementById("assetModal").classList.remove("show");
}

function openEditModal(id) {
  const asset = state.assets.find((item) => item.id === id);
  if (!asset) return;
  state.selectedAssetId = id;
  document.getElementById("modalTitle").textContent = "Edit asset";
  document.getElementById("assetName").value = asset.assetName;
  document.getElementById("tickerSymbol").value = asset.tickerSymbol;
  document.getElementById("category").value = asset.category;
  document.getElementById("quantity").value = asset.quantity;
  document.getElementById("buyPrice").value = asset.buyPrice;
  document.getElementById("currentPrice").value = asset.currentPrice;
  document.getElementById("riskLevel").value = asset.riskLevel;
  document.getElementById("notes").value = asset.notes || "";
  document.getElementById("assetModal").classList.add("show");
}

async function saveAsset(event) {
  event.preventDefault();
  const payload = {
    assetName: document.getElementById("assetName").value.trim(),
    tickerSymbol: document.getElementById("tickerSymbol").value.trim().toUpperCase(),
    category: document.getElementById("category").value,
    quantity: Number(document.getElementById("quantity").value),
    buyPrice: Number(document.getElementById("buyPrice").value),
    currentPrice: Number(document.getElementById("currentPrice").value),
    riskLevel: document.getElementById("riskLevel").value,
    notes: document.getElementById("notes").value.trim(),
  };

  try {
    if (state.selectedAssetId) {
      await request(`/assets/${state.selectedAssetId}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      });
      showToast("Asset updated successfully.");
    } else {
      await request("/assets", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      showToast("Asset added successfully.");
    }
    closeModal();
    await refreshTrackerTable();
  } catch (error) {
    showToast(error.message, "error");
  }
}

async function deleteAsset(id) {
  if (!confirm("Delete this asset from the tracker?")) return;
  try {
    await request(`/assets/${id}`, { method: "DELETE" });
    showToast("Asset deleted.");
    await refreshTrackerTable();
  } catch (error) {
    showToast(error.message, "error");
  }
}

function initDashboardChart() {
  const svg = document.getElementById("chartSvg");
  if (!svg) return;
  svg.innerHTML = `
    <defs>
      <linearGradient id="fillGradient" x1="0" x2="0" y1="0" y2="1">
        <stop offset="0%" stop-color="rgba(37,99,235,0.30)"></stop>
        <stop offset="100%" stop-color="rgba(37,99,235,0.03)"></stop>
      </linearGradient>
    </defs>
    <path d="M0 220 C 90 215, 120 180, 210 170 S 370 120, 440 145 S 580 110, 690 80 S 860 40, 960 50 L 960 320 L 0 320 Z" fill="url(#fillGradient)"></path>
    <path d="M0 220 C 90 215, 120 180, 210 170 S 370 120, 440 145 S 580 110, 690 80 S 860 40, 960 50" fill="none" stroke="#2563eb" stroke-width="5" stroke-linecap="round"></path>
  `;
}

function setTodayLabel() {
  const todayLabel = document.querySelector("[data-today-label]");
  if (!todayLabel) return;
  todayLabel.textContent = new Date().toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
    year: "numeric",
  });
}

function initPage() {
  const page = document.body.dataset.page;
  setTodayLabel();
  attachGlobalActions();
  if (page === "login") {
    generateCaptcha();
    document.getElementById("loginForm")?.addEventListener("submit", handleLogin);
    document.getElementById("refreshCaptcha")?.addEventListener("click", generateCaptcha);
  }
  if (page === "dashboard") {
    initDashboardChart();
    loadDashboard();
  }
  if (page === "tracker") {
    loadTracker();
  }
}

document.addEventListener("DOMContentLoaded", initPage);
window.openEditModal = openEditModal;
window.deleteAsset = deleteAsset;
window.closeModal = closeModal;
