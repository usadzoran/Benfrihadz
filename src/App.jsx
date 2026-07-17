import React, { useState, useEffect } from 'react'
import { 
  Search, 
  MapPin, 
  Star, 
  Briefcase, 
  Phone, 
  Lock, 
  User as UserIcon, 
  Database, 
  CheckCircle, 
  XCircle, 
  Plus, 
  Trash2, 
  LogOut, 
  ChevronLeft, 
  Award, 
  FileText, 
  Activity, 
  ExternalLink,
  MessageSquare,
  Sparkles,
  RefreshCw,
  Home as HomeIcon,
  Check
} from 'lucide-react'
import { db } from './supabase'
import confetti from 'canvas-confetti'

export default function App() {
  // Navigation State
  const [activeTab, setActiveTab] = useState('seeker') // 'seeker', 'provider_auth', 'provider_dash', 'admin_auth', 'admin_dash'
  
  // App Core Database States
  const [municipalities, setMunicipalities] = useState([])
  const [categories, setCategories] = useState([])
  const [providers, setProviders] = useState([])
  const [reviews, setReviews] = useState([])
  const [users, setUsers] = useState([])
  const [isSyncing, setIsSyncing] = useState(false)
  const [alert, setAlert] = useState(null)

  // Seeker Workspace States
  const [selectedCategory, setSelectedCategory] = useState(null)
  const [selectedMuni, setSelectedMuni] = useState('الكل')
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedProvider, setSelectedProvider] = useState(null) // Detailed View Modal

  // Review Form state
  const [newReviewName, setNewReviewName] = useState('')
  const [newReviewRating, setNewReviewRating] = useState(5)
  const [newReviewComment, setNewReviewComment] = useState('')

  // Provider Authentication States
  const [isProviderRegister, setIsProviderRegister] = useState(false)
  const [providerLoginPhone, setProviderLoginPhone] = useState('')
  const [providerLoginPass, setProviderLoginPass] = useState('')
  const [currentProviderUser, setCurrentProviderUser] = useState(null) // Auth Session

  // Provider Registration Form
  const [regFirstName, setRegFirstName] = useState('')
  const [regLastName, setRegLastName] = useState('')
  const [regMuni, setRegMuni] = useState('')
  const [regAddress, setRegAddress] = useState('')
  const [regPhone, setRegPhone] = useState('')
  const [regCategory, setRegCategory] = useState('')
  const [regExperience, setRegExperience] = useState(1)
  const [regShortDesc, setRegShortDesc] = useState('')
  const [regPassword, setRegPassword] = useState('')
  const [regWorkMunis, setRegWorkMunis] = useState([]) // Multi-select strings

  // Admin Authentication States
  const [adminUsername, setAdminUsername] = useState('')
  const [adminPassword, setAdminPassword] = useState('')
  const [isAdminLoggedIn, setIsAdminLoggedIn] = useState(false)
  const [adminActiveSubTab, setAdminActiveSubTab] = useState('pending') // 'pending', 'all_providers', 'categories', 'reviews'

  // Admin Panel Actions Form
  const [newMuniName, setNewMuniName] = useState('')
  const [newCatName, setNewCatName] = useState('')
  const [newCatIcon, setNewCatIcon] = useState('✨')

  // Load all initial database records
  const loadDatabase = async () => {
    try {
      const muniList = await db.getMunicipalities()
      const catList = await db.getCategories()
      const provList = await db.getProviders()
      const revList = await db.getReviews()
      const userList = await db.getUsers()

      setMunicipalities(muniList)
      setCategories(catList)
      setProviders(provList)
      setReviews(revList)
      setUsers(userList)
    } catch (e) {
      console.error("Error loading database: ", e)
    }
  }

  useEffect(() => {
    loadDatabase()
  }, [])

  // Auto-trigger impressions for approved craftspeople loaded on homepage
  useEffect(() => {
    if (activeTab === 'seeker') {
      const approvedOnScreen = providers.filter(p => p.isApproved === 1)
      approvedOnScreen.forEach(p => {
        db.incrementImpressions(p.id)
      })
    }
  }, [activeTab, providers])

  // Custom alert displayer
  const showAlert = (message, type = 'success') => {
    setAlert({ message, type })
    setTimeout(() => setAlert(null), 5000)
  }

  // Cloud Sync trigger
  const handleCloudSync = async () => {
    setIsSyncing(true)
    const result = await db.syncWithSupabase()
    setIsSyncing(false)
    if (result.success) {
      showAlert(result.message, 'success')
      loadDatabase()
    } else {
      showAlert(`فشلت المزامنة: ${result.error}`, 'error')
    }
  }

  // --- SEEKER FUNCTIONS ---
  const handleViewProviderDetail = (provider) => {
    setSelectedProvider(provider)
    db.incrementVisits(provider.id)
    // Update local state visits count
    setProviders(prev => prev.map(p => p.id === provider.id ? { ...p, visitsCount: (p.visitsCount || 0) + 1 } : p))
  }

  const handleSubmitReview = async (e) => {
    e.preventDefault()
    if (!newReviewName.trim() || !newReviewComment.trim()) {
      showAlert('يرجى ملء جميع الحقول لكتابة التقييم', 'error')
      return
    }

    const reviewObj = {
      providerId: selectedProvider.id,
      reviewerName: newReviewName,
      rating: newReviewRating,
      comment: newReviewComment
    }

    const added = await db.addReview(reviewObj)
    setReviews(prev => [...prev, added])
    
    // Success effects
    confetti({
      particleCount: 80,
      spread: 60,
      origin: { y: 0.7 }
    })
    
    showAlert('شكراً لك! تم نشر تقييمك بنجاح.')
    setNewReviewName('')
    setNewReviewComment('')
    setNewReviewRating(5)
  }

  // --- PROVIDER AUTH FUNCTIONS ---
  const handleProviderLogin = (e) => {
    e.preventDefault()
    const found = providers.find(p => p.phone === providerLoginPhone && p.password === providerLoginPass)
    if (found) {
      setCurrentProviderUser(found)
      setActiveTab('provider_dash')
      showAlert(`أهلاً بك مجدداً يا ${found.firstName}!`)
      setProviderLoginPhone('')
      setProviderLoginPass('')
    } else {
      showAlert('رقم الهاتف أو كلمة المرور غير صحيحة', 'error')
    }
  }

  const handleProviderRegister = async (e) => {
    e.preventDefault()
    if (!regFirstName || !regLastName || !regMuni || !regAddress || !regPhone || !regCategory || !regPassword) {
      showAlert('يرجى تعبئة جميع الحقول الإلزامية', 'error')
      return
    }

    // Check if phone already registered
    if (providers.some(p => p.phone === regPhone)) {
      showAlert('رقم الهاتف هذا مسجل بالفعل كحرفي', 'error')
      return
    }

    const newProvider = {
      firstName: regFirstName,
      lastName: regLastName,
      municipality: regMuni,
      address: regAddress,
      phone: regPhone,
      profilePic: `https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150`, // Standard placeholder
      serviceType: regCategory,
      shortDescription: regShortDesc,
      yearsOfExperience: parseInt(regExperience) || 1,
      municipalities: regWorkMunis.length > 0 ? regWorkMunis.join(',') : regMuni,
      password: regPassword
    }

    const registered = await db.registerProvider(newProvider)
    setProviders(prev => [...prev, registered])
    
    showAlert('تم تقديم طلب تسجيلك بنجاح! سيقوم المشرف بمراجعته قريباً.', 'success')
    setIsProviderRegister(false)
    
    // Clear registration state
    setRegFirstName('')
    setRegLastName('')
    setRegMuni('')
    setRegAddress('')
    setRegPhone('')
    setRegCategory('')
    setRegShortDesc('')
    setRegExperience(1)
    setRegWorkMunis([])
    setRegPassword('')
  }

  // --- ADMIN FUNCTIONS ---
  const handleAdminLogin = (e) => {
    e.preventDefault()
    if (adminUsername === 'admin' && adminPassword === 'admin') {
      setIsAdminLoggedIn(true)
      setActiveTab('admin_dash')
      showAlert('تم تسجيل الدخول كمسؤول رئيسي بنجاح')
    } else {
      showAlert('اسم المستخدم أو كلمة المرور للمسؤول غير صحيحة', 'error')
    }
  }

  const handleApproveProvider = async (id, approveStatus) => {
    await db.approveProvider(id, approveStatus)
    setProviders(prev => prev.map(p => p.id === id ? { ...p, isApproved: approveStatus } : p))
    showAlert(approveStatus === 1 ? 'تم قبول الحرفي بنجاح وتفعيل ملفه الشخصي!' : 'تم رفض طلب الحرفي وتحديث حالته.')
  }

  const handleDeleteProvider = async (id) => {
    if (window.confirm('هل أنت متأكد من رغبتك في حذف هذا الحرفي نهائياً؟')) {
      await db.deleteProvider(id)
      setProviders(prev => prev.filter(p => p.id !== id))
      showAlert('تم حذف حساب الحرفي من قاعدة البيانات')
    }
  }

  const handleAddMuni = async (e) => {
    e.preventDefault()
    if (!newMuniName.trim()) return
    if (municipalities.some(m => m.nameAr === newMuniName.trim())) {
      showAlert('هذه البلدية موجودة بالفعل', 'error')
      return
    }
    const added = await db.addMunicipality(newMuniName.trim())
    setMunicipalities(prev => [...prev, added])
    showAlert('تم إضافة البلدية الجديدة بنجاح!')
    setNewMuniName('')
  }

  const handleAddCat = async (e) => {
    e.preventDefault()
    if (!newCatName.trim()) return
    if (categories.some(c => c.nameAr === newCatName.trim())) {
      showAlert('هذا التصنيف موجود بالفعل', 'error')
      return
    }
    const added = await db.addCategory(newCatName.trim(), newCatIcon)
    setCategories(prev => [...prev, added])
    showAlert('تم إضافة تصنيف الخدمة الجديد بنجاح!')
    setNewCatName('')
    setNewCatIcon('✨')
  }

  const handleDeleteReview = async (id) => {
    if (window.confirm('هل أنت متأكد من رغبتك في حذف هذا التقييم؟')) {
      await db.deleteReview(id)
      setReviews(prev => prev.filter(r => r.id !== id))
      showAlert('تم حذف التقييم بنجاح')
    }
  }

  // --- FILTER LOGIC ---
  const filteredProviders = providers.filter(p => {
    // Must be approved for public view
    if (p.isApproved !== 1) return false
    
    // Category check
    if (selectedCategory && p.serviceType !== selectedCategory) return false

    // Municipality work area check
    if (selectedMuni !== 'الكل') {
      const areas = p.municipalities.split(',')
      if (!areas.includes(selectedMuni) && p.municipality !== selectedMuni) return false
    }

    // Search query match
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase()
      const fullName = `${p.firstName} ${p.lastName}`.toLowerCase()
      const matchesName = fullName.includes(q)
      const matchesDesc = p.shortDescription.toLowerCase().includes(q)
      const matchesType = p.serviceType.toLowerCase().includes(q)
      if (!matchesName && !matchesDesc && !matchesType) return false
    }

    return true
  })

  // Calculate Average Ratings
  const getProviderRating = (providerId) => {
    const provReviews = reviews.filter(r => r.providerId === providerId)
    if (provReviews.length === 0) return { avg: 5, count: 0 }
    const sum = provReviews.reduce((acc, curr) => acc + curr.rating, 0)
    return {
      avg: parseFloat((sum / provReviews.length).toFixed(1)),
      count: provReviews.length
    }
  }

  return (
    <div className="flex flex-col min-h-screen">
      
      {/* Alert Banner */}
      {alert && (
        <div className={`fixed top-4 left-4 right-4 md:left-auto md:w-96 z-50 p-4 rounded-xl shadow-2xl flex items-center gap-3 transition-all transform animate-bounce ${
          alert.type === 'error' ? 'bg-rose-600 text-white' : 'bg-emerald-600 text-white'
        }`}>
          {alert.type === 'error' ? <XCircle size={24} /> : <CheckCircle size={24} />}
          <p className="font-semibold text-sm">{alert.message}</p>
        </div>
      )}

      {/* Modern Header Navigation */}
      <header className="bg-white border-b border-slate-100 sticky top-0 z-40 backdrop-blur-md bg-white/90">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16 sm:h-20">
            
            {/* Logo and App Title */}
            <div 
              className="flex items-center gap-3 cursor-pointer select-none"
              onClick={() => { setActiveTab('seeker'); setSelectedProvider(null); setSelectedCategory(null); }}
            >
              <div className="w-10 h-10 sm:w-12 sm:h-12 bg-primary-600 text-white rounded-2xl flex items-center justify-center shadow-lg shadow-primary-200">
                <Briefcase size={22} className="stroke-[2.5]" />
              </div>
              <div>
                <h1 className="text-lg sm:text-2xl font-black text-slate-900 leading-none">خدمني وهران</h1>
                <span className="text-[10px] sm:text-xs text-primary-600 font-bold tracking-widest block mt-0.5">الدليل الحرفي المعتمد</span>
              </div>
            </div>

            {/* Right Header Navigation Actions */}
            <div className="flex items-center gap-2 sm:gap-4">
              
              {/* Database Sync Icon */}
              <button
                onClick={handleCloudSync}
                disabled={isSyncing}
                title="مزامنة مع السحابة"
                className={`p-2 rounded-xl border border-slate-100 hover:border-primary-100 hover:bg-primary-50 text-slate-600 hover:text-primary-600 transition-all ${
                  isSyncing ? 'animate-spin text-primary-600 bg-primary-50' : ''
                }`}
              >
                <RefreshCw size={18} />
              </button>

              {/* Main Tab Switchers */}
              <button 
                onClick={() => {
                  if (activeTab === 'seeker') {
                    if (currentProviderUser) {
                      setActiveTab('provider_dash')
                    } else {
                      setIsProviderRegister(false)
                      setActiveTab('provider_auth')
                    }
                  } else {
                    setActiveTab('seeker')
                  }
                  setSelectedProvider(null)
                }}
                className={`text-xs sm:text-sm font-bold py-2 sm:py-2.5 px-3 sm:px-5 rounded-xl transition-all flex items-center gap-2 ${
                  activeTab.startsWith('provider') 
                    ? 'bg-primary-600 text-white shadow-lg shadow-primary-200' 
                    : 'bg-slate-100 hover:bg-slate-200 text-slate-800'
                }`}
              >
                <Briefcase size={16} />
                <span className="hidden sm:inline">لوحة الحرفي 🛠️</span>
                <span className="sm:hidden">الحرفي</span>
              </button>

              <button 
                onClick={() => {
                  if (activeTab.startsWith('admin')) {
                    setActiveTab('seeker')
                  } else {
                    if (isAdminLoggedIn) {
                      setActiveTab('admin_dash')
                    } else {
                      setActiveTab('admin_auth')
                    }
                  }
                  setSelectedProvider(null)
                }}
                className={`text-xs sm:text-sm font-bold py-2 sm:py-2.5 px-3 sm:px-5 rounded-xl transition-all flex items-center gap-2 ${
                  activeTab.startsWith('admin') 
                    ? 'bg-amber-500 text-white shadow-lg shadow-amber-200' 
                    : 'bg-slate-100 hover:bg-slate-200 text-slate-800'
                }`}
              >
                <UserIcon size={16} />
                <span className="hidden sm:inline">لوحة الإدارة 👑</span>
                <span className="sm:hidden">الإدارة</span>
              </button>

            </div>
          </div>
        </div>
      </header>

      {/* Main Content Layout container */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-10">

        {/* ======================================================================== */}
        {/* VIEW 1: SEEKER WORKSPACE (CLIENT SEARCH & BROWSE PUBLIC AREA) */}
        {/* ======================================================================== */}
        {activeTab === 'seeker' && (
          <div className="space-y-8 animate-fadeIn">
            
            {/* Elegant Hero Visual Banner & Advanced Searching Form */}
            <div className="bg-gradient-to-br from-primary-900 to-slate-950 text-white rounded-3xl p-6 sm:p-12 shadow-2xl relative overflow-hidden">
              {/* Ambient Background Accents */}
              <div className="absolute top-0 right-0 w-80 h-80 bg-primary-600/15 rounded-full blur-3xl"></div>
              <div className="absolute bottom-0 left-0 w-80 h-80 bg-teal-500/10 rounded-full blur-3xl"></div>

              <div className="relative z-10 max-w-2xl text-right space-y-4">
                <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-primary-500/20 text-primary-300 border border-primary-500/30 text-xs font-bold">
                  <Sparkles size={14} />
                  <span>دليل حرفيي ولاية وهران الأكثر موثوقية</span>
                </div>
                <h2 className="text-2xl sm:text-4xl lg:text-5xl font-black leading-tight">
                  ابحث عن حرفي مؤهل لخدمتك المنزلية القادمة
                </h2>
                <p className="text-slate-300 text-sm sm:text-base leading-relaxed">
                  تواصل مباشرة مع نخبة من البنائين، السباكين، الكهربائيين والمصممين المحترفين في بلديات وهران، بئر الجير والسانية.
                </p>

                {/* Integrated Search Box & Filters */}
                <div className="pt-4 grid grid-cols-1 sm:grid-cols-12 gap-3">
                  <div className="sm:col-span-8 relative">
                    <Search className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                    <input 
                      type="text" 
                      placeholder="ابحث بالاسم، تصنيف العمل، أو الكلمات المفتاحية..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="w-full h-14 pr-12 pl-4 rounded-2xl bg-white text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-4 focus:ring-primary-500/40 text-sm font-semibold transition-all"
                    />
                  </div>

                  <div className="sm:col-span-4 relative">
                    <MapPin className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                    <select
                      value={selectedMuni}
                      onChange={(e) => setSelectedMuni(e.target.value)}
                      className="w-full h-14 pr-12 pl-8 rounded-2xl bg-white text-slate-800 font-semibold focus:outline-none focus:ring-4 focus:ring-primary-500/40 text-sm appearance-none cursor-pointer transition-all"
                    >
                      <option value="الكل">كل البلديات 📍</option>
                      {municipalities.map(m => (
                        <option key={m.id} value={m.nameAr}>{m.nameAr}</option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>
            </div>

            {/* Categories Selection Slider Row */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-black text-slate-900 flex items-center gap-2">
                  <span>اختر تصنيف الخدمة</span>
                  <span className="text-xs font-bold text-slate-400 bg-slate-100 px-2 py-0.5 rounded-md">{categories.length}</span>
                </h3>
                {selectedCategory && (
                  <button 
                    onClick={() => setSelectedCategory(null)}
                    className="text-xs text-primary-600 font-bold hover:underline"
                  >
                    إلغاء التصفية
                  </button>
                )}
              </div>
              
              <div className="flex gap-2.5 overflow-x-auto pb-3 snap-x scrollbar-none">
                <button
                  onClick={() => setSelectedCategory(null)}
                  className={`px-5 py-3 rounded-2xl font-bold text-sm whitespace-nowrap transition-all flex items-center gap-2 snap-center border ${
                    selectedCategory === null 
                      ? 'bg-primary-600 text-white border-primary-600 shadow-md shadow-primary-100' 
                      : 'bg-white text-slate-600 border-slate-100 hover:border-slate-200'
                  }`}
                >
                  💼 الكل
                </button>
                {categories.map(cat => (
                  <button
                    key={cat.id}
                    onClick={() => setSelectedCategory(cat.nameAr)}
                    className={`px-5 py-3 rounded-2xl font-bold text-sm whitespace-nowrap transition-all flex items-center gap-2 border snap-center ${
                      selectedCategory === cat.nameAr 
                        ? 'bg-primary-600 text-white border-primary-600 shadow-md shadow-primary-100' 
                        : 'bg-white text-slate-600 border-slate-100 hover:border-slate-200'
                    }`}
                  >
                    <span>{cat.iconName}</span>
                    <span>{cat.nameAr}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* List and Grid Results of Service Providers */}
            <div className="space-y-4">
              <h3 className="text-lg font-black text-slate-900">
                {filteredProviders.length === 0 ? 'لا توجد نتائج مطابقة' : `الحرفيون المتاحون في وهران (${filteredProviders.length})`}
              </h3>

              {filteredProviders.length === 0 ? (
                <div className="bg-white border border-slate-100 rounded-3xl p-10 text-center space-y-3">
                  <Briefcase size={40} className="mx-auto text-slate-300 stroke-[1.5]" />
                  <p className="text-slate-500 font-bold text-sm">نأسف، لم نجد أي حرفيين مسجلين وموافق عليهم يطابقون خيارات البحث الحالية.</p>
                  <p className="text-xs text-slate-400">يمكنك تبديل البلدية أو إلغاء تصفية التصنيف للبحث على نطاق أوسع.</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {filteredProviders.map(p => {
                    const ratingInfo = getProviderRating(p.id)
                    return (
                      <div 
                        key={p.id} 
                        className="bg-white rounded-3xl border border-slate-100 hover:border-primary-100 hover:shadow-xl transition-all overflow-hidden flex flex-col group"
                      >
                        {/* Upper Card Graphic Header */}
                        <div className="p-6 pb-4 border-b border-slate-50 flex gap-4">
                          <img 
                            src={p.profilePic || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"} 
                            alt={`${p.firstName} ${p.lastName}`}
                            className="w-16 h-16 rounded-2xl object-cover ring-4 ring-slate-50"
                          />
                          <div className="space-y-1">
                            <span className="px-2.5 py-0.5 rounded-full bg-slate-100 text-slate-600 text-[10px] font-bold inline-block">
                              {p.serviceType}
                            </span>
                            <h4 className="font-bold text-slate-900 group-hover:text-primary-600 transition-colors">
                              {p.firstName} {p.lastName}
                            </h4>
                            <div className="flex items-center gap-1.5 text-xs text-amber-500 font-bold">
                              <Star size={13} className="fill-amber-500" />
                              <span>{ratingInfo.avg}</span>
                              <span className="text-slate-400 font-medium">({ratingInfo.count} تقييم)</span>
                            </div>
                          </div>
                        </div>

                        {/* Mid Description Block */}
                        <div className="p-6 py-4 flex-1 space-y-3">
                          <p className="text-slate-500 text-xs sm:text-sm leading-relaxed line-clamp-3">
                            {p.shortDescription || 'لا يوجد وصف متاح لهذا الحرفي المحترف حتى الآن.'}
                          </p>

                          <div className="space-y-2 pt-2 text-xs font-bold text-slate-500">
                            <div className="flex items-center gap-2">
                              <MapPin size={14} className="text-primary-500" />
                              <span>المقر: {p.municipality}</span>
                            </div>
                            <div className="flex items-center gap-2">
                              <Award size={14} className="text-primary-500" />
                              <span>الخبرة: {p.yearsOfExperience} سنة</span>
                            </div>
                            <div className="flex items-center gap-1 flex-wrap">
                              <span className="text-slate-400">مناطق العمل: </span>
                              {p.municipalities.split(',').map((m, i) => (
                                <span key={i} className="bg-slate-100 px-1.5 py-0.5 rounded text-[10px] text-slate-600">{m}</span>
                              ))}
                            </div>
                          </div>
                        </div>

                        {/* Action Footer */}
                        <div className="p-6 pt-0">
                          <button 
                            onClick={() => handleViewProviderDetail(p)}
                            className="w-full py-3 rounded-2xl bg-slate-50 hover:bg-primary-50 hover:text-primary-600 text-slate-700 font-bold text-xs sm:text-sm transition-all flex items-center justify-center gap-1.5"
                          >
                            <span>عرض الملف والاتصال</span>
                            <ChevronLeft size={16} />
                          </button>
                        </div>
                      </div>
                    )
                  })}
                </div>
              )}
            </div>
          </div>
        )}

        {/* ======================================================================== */}
        {/* POPUP MODAL: DETAILED PROVIDER FILE & CALL / REVIEWING ACTION */}
        {/* ======================================================================== */}
        {selectedProvider && (
          <div className="fixed inset-0 bg-slate-900/60 z-50 flex items-center justify-center p-4 backdrop-blur-sm animate-fadeIn">
            <div className="bg-white rounded-3xl max-w-3xl w-full max-h-[90vh] overflow-y-auto shadow-2xl relative">
              
              {/* Close Button */}
              <button 
                onClick={() => setSelectedProvider(null)}
                className="absolute top-4 left-4 p-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-full transition-all"
              >
                <XCircle size={22} />
              </button>

              <div className="p-6 sm:p-10 space-y-8 text-right">
                
                {/* Header Information Card Area */}
                <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6 border-b border-slate-100 pb-8 pt-4">
                  <img 
                    src={selectedProvider.profilePic || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"} 
                    alt={`${selectedProvider.firstName} ${selectedProvider.lastName}`}
                    className="w-24 h-24 rounded-3xl object-cover ring-8 ring-slate-50"
                  />
                  
                  <div className="space-y-3 flex-1 text-center sm:text-right">
                    <div className="flex items-center justify-center sm:justify-start gap-2 flex-wrap">
                      <span className="px-3.5 py-1 rounded-full bg-primary-50 text-primary-600 text-xs font-extrabold border border-primary-100">
                        {selectedProvider.serviceType}
                      </span>
                      <span className="px-3.5 py-1 rounded-full bg-slate-100 text-slate-600 text-xs font-bold">
                        خبرة {selectedProvider.yearsOfExperience} سنة 🎖️
                      </span>
                    </div>

                    <h3 className="text-xl sm:text-2xl font-black text-slate-900">
                      {selectedProvider.firstName} {selectedProvider.lastName}
                    </h3>

                    <div className="flex items-center justify-center sm:justify-start gap-2.5 text-xs font-bold text-slate-500">
                      <div className="flex items-center gap-1">
                        <MapPin size={14} className="text-primary-500" />
                        <span>المقر: {selectedProvider.municipality}</span>
                      </div>
                      <span className="text-slate-300">|</span>
                      <span>تاريخ الانضمام: {new Date(selectedProvider.joinDate).toLocaleDateString('ar-DZ')}</span>
                    </div>

                    {/* Static stats displayed proudly */}
                    <div className="pt-2 flex items-center justify-center sm:justify-start gap-4 text-xs font-semibold text-slate-400">
                      <span>مشاهدات: {selectedProvider.impressionsCount} 👀</span>
                      <span>اتصالات: {selectedProvider.visitsCount} 📞</span>
                    </div>
                  </div>
                </div>

                {/* Grid Split Content columns */}
                <div className="grid grid-cols-1 md:grid-cols-12 gap-8">
                  
                  {/* Left Column: Reviews & Rating submission form */}
                  <div className="md:col-span-7 space-y-6">
                    <h4 className="text-lg font-black text-slate-900 flex items-center gap-2">
                      <MessageSquare size={18} className="text-primary-500" />
                      <span>تقييمات الزبائن والتعليقات</span>
                    </h4>

                    {/* List of existing Reviews */}
                    <div className="space-y-4 max-h-[300px] overflow-y-auto pr-2">
                      {reviews.filter(r => r.providerId === selectedProvider.id).length === 0 ? (
                        <p className="text-slate-400 text-xs font-bold py-6 text-center">لا توجد تقييمات لهذا الحرفي بعد. كن أول من يقيمه!</p>
                      ) : (
                        reviews.filter(r => r.providerId === selectedProvider.id).map(rev => (
                          <div key={rev.id} className="p-4 bg-slate-50 rounded-2xl space-y-1.5 text-xs sm:text-sm border border-slate-100">
                            <div className="flex items-center justify-between">
                              <span className="font-bold text-slate-900">{rev.reviewerName}</span>
                              <div className="flex items-center gap-0.5 text-amber-500">
                                {[...Array(rev.rating)].map((_, i) => (
                                  <Star key={i} size={12} className="fill-amber-500" />
                                ))}
                              </div>
                            </div>
                            <p className="text-slate-600 leading-relaxed font-semibold">{rev.comment}</p>
                            <span className="text-[10px] text-slate-400 block pt-1">
                              {new Date(rev.timestamp).toLocaleDateString('ar-DZ')}
                            </span>
                          </div>
                        ))
                      )}
                    </div>

                    {/* Write new review form */}
                    <form onSubmit={handleSubmitReview} className="p-5 border border-slate-100 rounded-2xl bg-white space-y-4">
                      <h5 className="font-extrabold text-sm text-slate-800">أضف تقييمك وتجربتك الشخصية</h5>
                      
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div className="space-y-1.5">
                          <label className="text-xs font-extrabold text-slate-600 block">اسمك الكريم</label>
                          <input 
                            type="text" 
                            required
                            placeholder="مثال: محمد الجزائري"
                            value={newReviewName}
                            onChange={(e) => setNewReviewName(e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-slate-100 focus:outline-none focus:ring-4 focus:ring-primary-500/10 text-xs font-bold"
                          />
                        </div>

                        <div className="space-y-1.5">
                          <label className="text-xs font-extrabold text-slate-600 block">التقييم بالنجوم</label>
                          <select
                            value={newReviewRating}
                            onChange={(e) => setNewReviewRating(parseInt(e.target.value))}
                            className="w-full px-4 py-2.5 rounded-xl border border-slate-100 focus:outline-none text-xs font-extrabold appearance-none cursor-pointer"
                          >
                            <option value="5">⭐⭐⭐⭐⭐ ممتاز (5/5)</option>
                            <option value="4">⭐⭐⭐⭐ جيد جداً (4/5)</option>
                            <option value="3">⭐⭐⭐ متوسط (3/5)</option>
                            <option value="2">⭐⭐ مقبول (2/5)</option>
                            <option value="1">⭐ ضعيف جداً (1/5)</option>
                          </select>
                        </div>
                      </div>

                      <div className="space-y-1.5">
                        <label className="text-xs font-extrabold text-slate-600 block">تفاصيل تجربتك مع الحرفي</label>
                        <textarea
                          rows="3"
                          required
                          placeholder="اكتب انطباعك بصدق وأمانة لمساعدة الآخرين في الاختيار..."
                          value={newReviewComment}
                          onChange={(e) => setNewReviewComment(e.target.value)}
                          className="w-full px-4 py-2.5 rounded-xl border border-slate-100 focus:outline-none focus:ring-4 focus:ring-primary-500/10 text-xs font-semibold leading-relaxed"
                        ></textarea>
                      </div>

                      <button 
                        type="submit"
                        className="w-full py-2.5 rounded-xl bg-primary-600 hover:bg-primary-700 text-white font-bold text-xs transition-all shadow-md shadow-primary-100"
                      >
                        إرسال التقييم
                      </button>
                    </form>
                  </div>

                  {/* Right Column: Descriptions, working munis & action connections */}
                  <div className="md:col-span-5 space-y-6">
                    <div className="space-y-3">
                      <h4 className="text-base font-extrabold text-slate-800">نبذة تعريفية وعملية</h4>
                      <p className="text-slate-500 text-xs sm:text-sm leading-relaxed bg-slate-50 p-4 rounded-2xl border border-slate-100">
                        {selectedProvider.shortDescription || 'لا توجد نبذة تفصيلية متاحة بعد.'}
                      </p>
                    </div>

                    <div className="space-y-3">
                      <h4 className="text-base font-extrabold text-slate-800">العنوان الدقيق ومناطق التغطية</h4>
                      <div className="p-4 rounded-2xl border border-slate-100 space-y-2 text-xs font-semibold">
                        <p className="text-slate-600">🏠 العنوان: <span className="font-bold">{selectedProvider.address}</span></p>
                        <div className="flex flex-wrap items-center gap-1.5 pt-1">
                          <span className="text-slate-400">نغطي خدماتنا في:</span>
                          {selectedProvider.municipalities.split(',').map((m, i) => (
                            <span key={i} className="bg-primary-50 text-primary-600 px-2 py-0.5 rounded-md font-bold text-[10px]">{m}</span>
                          ))}
                        </div>
                      </div>
                    </div>

                    {/* BIG PROFESSIONAL CONTACT BUTTONS */}
                    <div className="pt-4 space-y-3">
                      <a 
                        href={`tel:${selectedProvider.phone}`}
                        className="w-full py-4 rounded-2xl bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold text-sm sm:text-base transition-all flex items-center justify-center gap-2.5 shadow-lg shadow-emerald-100"
                      >
                        <Phone size={18} />
                        <span>اتصل الآن: {selectedProvider.phone}</span>
                      </a>

                      <a 
                        href={`https://wa.me/213${selectedProvider.phone.replace(/^0/, '')}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="w-full py-4 rounded-2xl bg-slate-900 hover:bg-slate-800 text-white font-bold text-sm transition-all flex items-center justify-center gap-2"
                      >
                        <MessageSquare size={16} />
                        <span>أرسل رسالة واتساب مباشرة</span>
                        <ExternalLink size={14} />
                      </a>
                    </div>
                  </div>

                </div>

              </div>
            </div>
          </div>
        )}

        {/* ======================================================================== */}
        {/* VIEW 2: PROVIDER PORTAL / AUTHENTICATION */}
        {/* ======================================================================== */}
        {activeTab === 'provider_auth' && (
          <div className="max-w-md mx-auto bg-white rounded-3xl border border-slate-100 shadow-2xl p-6 sm:p-10 text-right animate-fadeIn">
            
            {/* Header Switcher */}
            <div className="text-center space-y-2 mb-8">
              <div className="w-12 h-12 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center mx-auto">
                <Briefcase size={24} />
              </div>
              <h3 className="text-xl font-black text-slate-900">
                {isProviderRegister ? 'انضم كحرفي محترف وشريك' : 'لوحة تحكم الحرفيين'}
              </h3>
              <p className="text-slate-400 text-xs font-semibold">
                {isProviderRegister ? 'املأ معلوماتك لتقديم طلب انضمامك لقائمة الخدمات المعتمدة' : 'سجل دخولك لتعديل ملفك ومشاهدة إحصائياتك'}
              </p>
            </div>

            {/* IF PROVIDER WANTS TO LOGIN */}
            {!isProviderRegister ? (
              <form onSubmit={handleProviderLogin} className="space-y-5">
                <div className="space-y-1.5">
                  <label className="text-xs font-extrabold text-slate-600 block">رقم الهاتف المسجل</label>
                  <input 
                    type="tel" 
                    required
                    placeholder="مثال: 0555123456"
                    value={providerLoginPhone}
                    onChange={(e) => setProviderLoginPhone(e.target.value)}
                    className="w-full px-4 py-3 rounded-2xl border border-slate-100 focus:outline-none focus:ring-4 focus:ring-primary-500/10 text-xs sm:text-sm font-bold"
                  />
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-extrabold text-slate-600 block">كلمة المرور الحاصة بك</label>
                  <input 
                    type="password" 
                    required
                    placeholder="••••••••"
                    value={providerLoginPass}
                    onChange={(e) => setProviderLoginPass(e.target.value)}
                    className="w-full px-4 py-3 rounded-2xl border border-slate-100 focus:outline-none focus:ring-4 focus:ring-primary-500/10 text-xs sm:text-sm font-bold"
                  />
                </div>

                <button 
                  type="submit"
                  className="w-full py-3.5 rounded-2xl bg-primary-600 hover:bg-primary-700 text-white font-extrabold text-sm transition-all shadow-lg shadow-primary-200"
                >
                  تسجيل الدخول للوحة التحكم
                </button>

                <div className="pt-4 text-center">
                  <button 
                    type="button"
                    onClick={() => setIsProviderRegister(true)}
                    className="text-xs text-primary-600 font-extrabold hover:underline"
                  >
                    ليس لديك حساب حرفي؟ قدم طلب انضمام الآن
                  </button>
                </div>
              </form>
            ) : (
              /* IF PROVIDER WANTS TO REGISTER */
              <form onSubmit={handleProviderRegister} className="space-y-4">
                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-1">
                    <label className="text-[10px] sm:text-xs font-extrabold text-slate-600 block">الاسم الأول *</label>
                    <input 
                      type="text" 
                      required
                      placeholder="أحمد"
                      value={regFirstName}
                      onChange={(e) => setRegFirstName(e.target.value)}
                      className="w-full px-3 py-2.5 rounded-xl border border-slate-100 focus:outline-none text-xs font-bold"
                    />
                  </div>
                  <div className="space-y-1">
                    <label className="text-[10px] sm:text-xs font-extrabold text-slate-600 block">اللقب/العائلة *</label>
                    <input 
                      type="text" 
                      required
                      placeholder="بلقاسم"
                      value={regLastName}
                      onChange={(e) => setRegLastName(e.target.value)}
                      className="w-full px-3 py-2.5 rounded-xl border border-slate-100 focus:outline-none text-xs font-bold"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-1">
                    <label className="text-[10px] sm:text-xs font-extrabold text-slate-600 block">البلدية الرئيسية *</label>
                    <select
                      required
                      value={regMuni}
                      onChange={(e) => setRegMuni(e.target.value)}
                      className="w-full px-3 py-2.5 rounded-xl border border-slate-100 focus:outline-none text-xs font-extrabold cursor-pointer"
                    >
                      <option value="">اختر...</option>
                      {municipalities.map(m => (
                        <option key={m.id} value={m.nameAr}>{m.nameAr}</option>
                      ))}
                    </select>
                  </div>
                  <div className="space-y-1">
                    <label className="text-[10px] sm:text-xs font-extrabold text-slate-600 block">تصنيف المهنة/الخدمة *</label>
                    <select
                      required
                      value={regCategory}
                      onChange={(e) => setRegCategory(e.target.value)}
                      className="w-full px-3 py-2.5 rounded-xl border border-slate-100 focus:outline-none text-xs font-extrabold cursor-pointer"
                    >
                      <option value="">اختر...</option>
                      {categories.map(c => (
                        <option key={c.id} value={c.nameAr}>{c.nameAr}</option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="space-y-1">
                  <label className="text-[10px] sm:text-xs font-extrabold text-slate-600 block">العنوان الدقيق بالتفصيل *</label>
                  <input 
                    type="text" 
                    required
                    placeholder="مثال: حي الياسمين عمارة ب رقم 5، وهران"
                    value={regAddress}
                    onChange={(e) => setRegAddress(e.target.value)}
                    className="w-full px-3 py-2.5 rounded-xl border border-slate-100 focus:outline-none text-xs font-semibold"
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-1">
                    <label className="text-[10px] sm:text-xs font-extrabold text-slate-600 block">رقم الهاتف الجوال *</label>
                    <input 
                      type="tel" 
                      required
                      placeholder="0555123456"
                      value={regPhone}
                      onChange={(e) => setRegPhone(e.target.value)}
                      className="w-full px-3 py-2.5 rounded-xl border border-slate-100 focus:outline-none text-xs font-bold"
                    />
                  </div>
                  <div className="space-y-1">
                    <label className="text-[10px] sm:text-xs font-extrabold text-slate-600 block">سنوات الخبرة العملية *</label>
                    <input 
                      type="number" 
                      required
                      min="1"
                      placeholder="5"
                      value={regExperience}
                      onChange={(e) => setRegExperience(e.target.value)}
                      className="w-full px-3 py-2.5 rounded-xl border border-slate-100 focus:outline-none text-xs font-bold"
                    />
                  </div>
                </div>

                {/* Multiple Munis Coverage Selector */}
                <div className="space-y-1.5">
                  <label className="text-[10px] sm:text-xs font-extrabold text-slate-600 block">مناطق التغطية والعمل الإضافية</label>
                  <div className="flex flex-wrap gap-1.5 p-2 bg-slate-50 rounded-xl border border-slate-100">
                    {municipalities.map(m => {
                      const active = regWorkMunis.includes(m.nameAr)
                      return (
                        <button
                          key={m.id}
                          type="button"
                          onClick={() => {
                            if (active) {
                              setRegWorkMunis(prev => prev.filter(i => i !== m.nameAr))
                            } else {
                              setRegWorkMunis(prev => [...prev, m.nameAr])
                            }
                          }}
                          className={`px-2.5 py-1 rounded-lg text-[10px] font-extrabold border transition-all flex items-center gap-1 ${
                            active 
                              ? 'bg-primary-600 border-primary-600 text-white' 
                              : 'bg-white border-slate-100 text-slate-600'
                          }`}
                        >
                          {active && <Check size={10} />}
                          <span>{m.nameAr}</span>
                        </button>
                      )
                    })}
                  </div>
                </div>

                <div className="space-y-1">
                  <label className="text-[10px] sm:text-xs font-extrabold text-slate-600 block">نبذة مختصرة تظهر للزبائن</label>
                  <textarea
                    rows="2"
                    placeholder="اكتب لمحة مبسطة عن خدماتك الاستثنائية..."
                    value={regShortDesc}
                    onChange={(e) => setRegShortDesc(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-100 focus:outline-none text-xs font-semibold leading-relaxed"
                  ></textarea>
                </div>

                <div className="space-y-1">
                  <label className="text-[10px] sm:text-xs font-extrabold text-slate-600 block">رمز المرور الخاص بملفك الحرفي *</label>
                  <input 
                    type="password" 
                    required
                    placeholder="••••••••"
                    value={regPassword}
                    onChange={(e) => setRegPassword(e.target.value)}
                    className="w-full px-3 py-2.5 rounded-xl border border-slate-100 focus:outline-none text-xs font-bold"
                  />
                </div>

                <button 
                  type="submit"
                  className="w-full py-3 rounded-xl bg-primary-600 hover:bg-primary-700 text-white font-extrabold text-xs transition-all shadow-md shadow-primary-100"
                >
                  تسجيل وتقديم طلب انضمام
                </button>

                <div className="pt-2 text-center">
                  <button 
                    type="button"
                    onClick={() => setIsProviderRegister(false)}
                    className="text-[11px] text-slate-400 font-bold hover:underline"
                  >
                    لديك حساب بالفعل؟ تسجيل الدخول
                  </button>
                </div>
              </form>
            )}

          </div>
        )}

        {/* ======================================================================== */}
        {/* VIEW 3: ACTIVE PROVIDER DASHBOARD (AUTHENTICATED) */}
        {/* ======================================================================== */}
        {activeTab === 'provider_dash' && currentProviderUser && (
          <div className="space-y-8 text-right animate-fadeIn">
            
            {/* Header section with Welcome statement */}
            <div className="bg-white rounded-3xl border border-slate-100 p-6 sm:p-8 flex flex-col sm:flex-row items-center justify-between gap-6">
              <div className="flex items-center gap-4 text-right">
                <img 
                  src={currentProviderUser.profilePic || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"} 
                  alt="Harpist" 
                  className="w-16 h-16 rounded-2xl object-cover"
                />
                <div className="space-y-1">
                  <h3 className="text-xl font-black text-slate-900">
                    أهلاً بك يا {currentProviderUser.firstName} {currentProviderUser.lastName} 👋
                  </h3>
                  <div className="flex items-center gap-2 text-xs font-bold">
                    <span className="text-slate-400">حالة الحساب:</span>
                    {currentProviderUser.isApproved === 1 ? (
                      <span className="text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded">نشط / موافق عليه ✅</span>
                    ) : currentProviderUser.isApproved === 0 ? (
                      <span className="text-amber-500 bg-amber-50 px-2 py-0.5 rounded">قيد المراجعة والتدقيق 🕒</span>
                    ) : (
                      <span className="text-rose-600 bg-rose-50 px-2 py-0.5 rounded">مرفوض ❌</span>
                    )}
                  </div>
                </div>
              </div>

              <button 
                onClick={() => {
                  setCurrentProviderUser(null)
                  setActiveTab('seeker')
                  showAlert('تم تسجيل الخروج بنجاح')
                }}
                className="py-2.5 px-5 rounded-xl border border-rose-100 hover:bg-rose-50 text-rose-600 font-bold text-xs flex items-center gap-1.5 transition-all"
              >
                <LogOut size={16} />
                <span>تسجيل الخروج</span>
              </button>
            </div>

            {/* Dash Analytics Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              
              <div className="bg-white rounded-3xl border border-slate-100 p-6 space-y-3 relative overflow-hidden">
                <div className="absolute top-0 left-0 w-16 h-16 bg-primary-50 rounded-br-full flex items-center justify-center">
                  <Activity size={20} className="text-primary-600" />
                </div>
                <h4 className="text-xs font-extrabold text-slate-400">إجمالي مشاهدات الملف</h4>
                <p className="text-3xl font-black text-slate-950">{currentProviderUser.impressionsCount || 0}</p>
                <span className="text-[10px] text-slate-400 block font-semibold">كم مرة تم عرض بطاقتك الشخصية</span>
              </div>

              <div className="bg-white rounded-3xl border border-slate-100 p-6 space-y-3 relative overflow-hidden">
                <div className="absolute top-0 left-0 w-16 h-16 bg-emerald-50 rounded-br-full flex items-center justify-center">
                  <Phone size={20} className="text-emerald-600" />
                </div>
                <h4 className="text-xs font-extrabold text-slate-400">زيارات الملف والاتصال</h4>
                <p className="text-3xl font-black text-slate-950">{currentProviderUser.visitsCount || 0}</p>
                <span className="text-[10px] text-slate-400 block font-semibold">كم زبوناً نقر لمشاهدة تفاصيلك والاتصال بك</span>
              </div>

              <div className="bg-white rounded-3xl border border-slate-100 p-6 space-y-3 relative overflow-hidden">
                <div className="absolute top-0 left-0 w-16 h-16 bg-amber-50 rounded-br-full flex items-center justify-center">
                  <Star size={20} className="text-amber-500 fill-amber-500" />
                </div>
                <h4 className="text-xs font-extrabold text-slate-400">التقييم العام</h4>
                <p className="text-3xl font-black text-slate-950">
                  {getProviderRating(currentProviderUser.id).avg} / 5
                </p>
                <span className="text-[10px] text-slate-400 block font-semibold">معدل تقييمات الزبائن ({getProviderRating(currentProviderUser.id).count} تقييم)</span>
              </div>

            </div>

            {/* Custom Information edit profile panel */}
            <div className="bg-white rounded-3xl border border-slate-100 p-6 sm:p-8 space-y-6">
              <h4 className="text-lg font-black text-slate-900 border-b border-slate-50 pb-3">تحديث وتعديل ملفك الحرفي</h4>
              
              <div className="p-4 bg-amber-50 text-amber-700 rounded-2xl text-xs font-extrabold leading-relaxed border border-amber-100">
                ⚠️ ملاحظة: أي تغييرات تجريها على تصنيفك أو منطقتك الأساسية ستتطلب إعادة الموافقة والتدقيق من قبل فريق الإدارة لضمان جودة ومصداقية الموقع.
              </div>

              {/* Form editing simulation */}
              <form onSubmit={async (e) => {
                e.preventDefault()
                showAlert('تم حفظ تعديلات ملفك الشخصي وتحديثها بنجاح!')
              }} className="grid grid-cols-1 md:grid-cols-2 gap-6">
                
                <div className="space-y-1.5">
                  <label className="text-xs font-extrabold text-slate-600 block">رقم هاتف الاتصال للزبائن</label>
                  <input 
                    type="tel" 
                    value={currentProviderUser.phone}
                    disabled
                    className="w-full px-4 py-3 rounded-2xl border border-slate-100 bg-slate-50 text-slate-400 text-xs sm:text-sm font-bold cursor-not-allowed"
                  />
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-extrabold text-slate-600 block">العنوان الجغرافي المعروض للعمل</label>
                  <input 
                    type="text" 
                    value={currentProviderUser.address}
                    onChange={(e) => {
                      const v = e.target.value
                      setCurrentProviderUser(p => ({ ...p, address: v }))
                    }}
                    className="w-full px-4 py-3 rounded-2xl border border-slate-100 text-xs sm:text-sm font-bold"
                  />
                </div>

                <div className="space-y-1.5 md:col-span-2">
                  <label className="text-xs font-extrabold text-slate-600 block">النبذة المهنية المعروضة للزبائن</label>
                  <textarea
                    rows="3"
                    value={currentProviderUser.shortDescription}
                    onChange={(e) => {
                      const v = e.target.value
                      setCurrentProviderUser(p => ({ ...p, shortDescription: v }))
                    }}
                    className="w-full px-4 py-3 rounded-2xl border border-slate-100 text-xs sm:text-sm font-semibold leading-relaxed"
                  ></textarea>
                </div>

                <div className="md:col-span-2">
                  <button 
                    type="submit"
                    className="py-3 px-8 bg-primary-600 hover:bg-primary-700 text-white font-extrabold text-xs sm:text-sm rounded-xl transition-all shadow-md shadow-primary-100"
                  >
                    حفظ التعديلات الحالية
                  </button>
                </div>

              </form>
            </div>

          </div>
        )}

        {/* ======================================================================== */}
        {/* VIEW 4: ADMIN PORTAL / AUTHENTICATION */}
        {/* ======================================================================== */}
        {activeTab === 'admin_auth' && (
          <div className="max-w-md mx-auto bg-white rounded-3xl border border-slate-100 shadow-2xl p-6 sm:p-10 text-right animate-fadeIn">
            
            <div className="text-center space-y-2 mb-8">
              <div className="w-12 h-12 bg-amber-100 text-amber-500 rounded-full flex items-center justify-center mx-auto">
                <Lock size={24} />
              </div>
              <h3 className="text-xl font-black text-slate-900">تسجيل الدخول للمشرفين</h3>
              <p className="text-slate-400 text-xs font-semibold">تتطلب هذه المنطقة صلاحيات الإدارة العليا للولاية</p>
            </div>

            <form onSubmit={handleAdminLogin} className="space-y-5">
              <div className="space-y-1.5">
                <label className="text-xs font-extrabold text-slate-600 block">اسم مستخدم المسؤول</label>
                <input 
                  type="text" 
                  required
                  placeholder="مثال: admin"
                  value={adminUsername}
                  onChange={(e) => setAdminUsername(e.target.value)}
                  className="w-full px-4 py-3 rounded-2xl border border-slate-100 focus:outline-none focus:ring-4 focus:ring-amber-500/10 text-xs sm:text-sm font-bold"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-extrabold text-slate-600 block">كلمة المرور الخاصة بك</label>
                <input 
                  type="password" 
                  required
                  placeholder="••••••••"
                  value={adminPassword}
                  onChange={(e) => setAdminPassword(e.target.value)}
                  className="w-full px-4 py-3 rounded-2xl border border-slate-100 focus:outline-none focus:ring-4 focus:ring-amber-500/10 text-xs sm:text-sm font-bold"
                />
              </div>

              <button 
                type="submit"
                className="w-full py-3.5 rounded-2xl bg-amber-500 hover:bg-amber-600 text-white font-extrabold text-sm transition-all shadow-lg shadow-amber-200"
              >
                الدخول كمسؤول رئيسي
              </button>
            </form>

          </div>
        )}

        {/* ======================================================================== */}
        {/* VIEW 5: FULL-FEATURED ADMIN PANEL (AUTHENTICATED) */}
        {/* ======================================================================== */}
        {activeTab === 'admin_dash' && isAdminLoggedIn && (
          <div className="space-y-8 text-right animate-fadeIn">
            
            {/* Header section admin area */}
            <div className="bg-slate-900 text-white rounded-3xl p-6 sm:p-8 flex flex-col sm:flex-row items-center justify-between gap-6">
              <div className="space-y-1">
                <h3 className="text-lg sm:text-xl font-black flex items-center gap-2">
                  <span>لوحة التحكم الرئيسية للمشرف الرئيسي</span>
                  <span className="text-[10px] font-extrabold bg-amber-500 text-slate-900 px-2.5 py-0.5 rounded-full">التحكم الكلي</span>
                </h3>
                <p className="text-slate-400 text-xs font-bold">إدارة طلبات الحرفيين، تصنيفات المهن، المراجعات والبلديات لولاية وهران.</p>
              </div>

              <div className="flex items-center gap-3">
                <button 
                  onClick={handleCloudSync}
                  className="py-2.5 px-4 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl font-bold text-xs flex items-center gap-1.5 transition-all"
                >
                  <RefreshCw size={14} />
                  <span>مزامنة سحابية كاملة</span>
                </button>

                <button 
                  onClick={() => {
                    setIsAdminLoggedIn(false)
                    setActiveTab('seeker')
                    showAlert('تم تسجيل الخروج من لوحة الإدارة')
                  }}
                  className="py-2.5 px-4 bg-rose-600/20 hover:bg-rose-600 text-rose-200 hover:text-white rounded-xl font-bold text-xs flex items-center gap-1.5 transition-all"
                >
                  <LogOut size={14} />
                  <span>خروج المشرف</span>
                </button>
              </div>
            </div>

            {/* Sub navigation for Admin views */}
            <div className="flex flex-wrap gap-2 border-b border-slate-100 pb-3">
              <button
                onClick={() => setAdminActiveSubTab('pending')}
                className={`py-2 px-4 rounded-xl text-xs font-black transition-all flex items-center gap-1.5 ${
                  adminActiveSubTab === 'pending'
                    ? 'bg-amber-500 text-white shadow-md'
                    : 'bg-white text-slate-600 hover:bg-slate-50 border border-slate-100'
                }`}
              >
                <span>طلبات الانضمام المعلقة</span>
                <span className="bg-amber-100 text-amber-800 text-[10px] px-1.5 py-0.5 rounded font-black">
                  {providers.filter(p => p.isApproved === 0).length}
                </span>
              </button>

              <button
                onClick={() => setAdminActiveSubTab('all_providers')}
                className={`py-2 px-4 rounded-xl text-xs font-black transition-all flex items-center gap-1.5 ${
                  adminActiveSubTab === 'all_providers'
                    ? 'bg-primary-600 text-white shadow-md'
                    : 'bg-white text-slate-600 hover:bg-slate-50 border border-slate-100'
                }`}
              >
                <span>جميع الحرفيين المسجلين</span>
                <span className="bg-primary-100 text-primary-800 text-[10px] px-1.5 py-0.5 rounded font-black">
                  {providers.length}
                </span>
              </button>

              <button
                onClick={() => setAdminActiveSubTab('categories')}
                className={`py-2 px-4 rounded-xl text-xs font-black transition-all flex items-center gap-1.5 ${
                  adminActiveSubTab === 'categories'
                    ? 'bg-slate-800 text-white shadow-md'
                    : 'bg-white text-slate-600 hover:bg-slate-50 border border-slate-100'
                }`}
              >
                <span>التصنيفات والبلديات</span>
              </button>

              <button
                onClick={() => setAdminActiveSubTab('reviews')}
                className={`py-2 px-4 rounded-xl text-xs font-black transition-all flex items-center gap-1.5 ${
                  adminActiveSubTab === 'reviews'
                    ? 'bg-rose-600 text-white shadow-md'
                    : 'bg-white text-slate-600 hover:bg-slate-50 border border-slate-100'
                }`}
              >
                <span>إدارة التقييمات الحديثة</span>
                <span className="bg-rose-100 text-rose-800 text-[10px] px-1.5 py-0.5 rounded font-black">
                  {reviews.length}
                </span>
              </button>
            </div>

            {/* ========================================== */}
            {/* SUB-VIEW A: PENDING CRAFTSMEN QUEUE */}
            {/* ========================================== */}
            {adminActiveSubTab === 'pending' && (
              <div className="space-y-4">
                <h4 className="text-base font-black text-slate-900">طلبات الحرفيين المعلقة بانتظار الموافقة</h4>
                
                {providers.filter(p => p.isApproved === 0).length === 0 ? (
                  <div className="p-8 bg-white border border-slate-100 rounded-3xl text-center text-slate-400 font-bold text-xs sm:text-sm">
                    ✅ لا توجد أي طلبات انضمام جديدة معلقة في الوقت الحالي.
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {providers.filter(p => p.isApproved === 0).map(p => (
                      <div key={p.id} className="bg-white rounded-3xl border border-slate-100 p-5 space-y-4 flex flex-col">
                        
                        <div className="flex gap-4 border-b border-slate-50 pb-3">
                          <img 
                            src={p.profilePic || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"} 
                            alt="Pending Avatar"
                            className="w-14 h-14 rounded-2xl object-cover ring-2 ring-slate-100"
                          />
                          <div className="space-y-1">
                            <span className="px-2 py-0.5 bg-amber-50 text-amber-600 rounded text-[10px] font-bold inline-block">
                              {p.serviceType}
                            </span>
                            <h5 className="font-extrabold text-sm text-slate-900">{p.firstName} {p.lastName}</h5>
                            <p className="text-[11px] text-slate-400 font-bold">الهاتف: {p.phone}</p>
                          </div>
                        </div>

                        <div className="flex-1 space-y-2 text-xs font-semibold text-slate-500">
                          <p>🏠 العنوان المعنون: <span className="text-slate-800 font-bold">{p.address}</span></p>
                          <p>📍 البلدية المحددة: <span className="text-slate-800 font-bold">{p.municipality}</span></p>
                          <p>🎖️ سنوات الخبرة: <span className="text-slate-800 font-bold">{p.yearsOfExperience} سنوات</span></p>
                          <p className="bg-slate-50 p-2.5 rounded-xl text-slate-500 text-[11px] leading-relaxed italic">
                            &quot;{p.shortDescription || 'لا يوجد وصف متاح.'}&quot;
                          </p>
                        </div>

                        <div className="grid grid-cols-2 gap-3 pt-2">
                          <button
                            onClick={() => handleApproveProvider(p.id, 1)}
                            className="py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs transition-all flex items-center justify-center gap-1"
                          >
                            <CheckCircle size={14} />
                            <span>قبول وتفعيل</span>
                          </button>
                          <button
                            onClick={() => handleApproveProvider(p.id, -1)}
                            className="py-2.5 rounded-xl bg-rose-600/10 hover:bg-rose-600 text-rose-700 hover:text-white font-bold text-xs transition-all flex items-center justify-center gap-1"
                          >
                            <XCircle size={14} />
                            <span>رفض الطلب</span>
                          </button>
                        </div>

                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* ========================================== */}
            {/* SUB-VIEW B: ALL REGISTERED PROVIDERS */}
            {/* ========================================== */}
            {adminActiveSubTab === 'all_providers' && (
              <div className="bg-white border border-slate-100 rounded-3xl overflow-hidden shadow-sm text-xs sm:text-sm">
                <div className="p-6 border-b border-slate-50">
                  <h4 className="font-black text-slate-900">سجل جميع الحرفيين وشركاء الخدمة</h4>
                </div>

                <div className="overflow-x-auto">
                  <table className="w-full text-right border-collapse">
                    <thead>
                      <tr className="bg-slate-50 text-slate-400 font-bold border-b border-slate-100 text-xs">
                        <th className="p-4">الحرفي</th>
                        <th className="p-4">التصنيف</th>
                        <th className="p-4">البلدية</th>
                        <th className="p-4">رقم الهاتف</th>
                        <th className="p-4">الحالة</th>
                        <th className="p-4">الإحصائيات</th>
                        <th className="p-4 text-center">إجراءات</th>
                      </tr>
                    </thead>
                    <tbody className="font-semibold text-slate-700">
                      {providers.map(p => (
                        <tr key={p.id} className="border-b border-slate-50 hover:bg-slate-50/50 transition-colors">
                          <td className="p-4 flex items-center gap-3">
                            <img 
                              src={p.profilePic || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"} 
                              alt="Avatar" 
                              className="w-8 h-8 rounded-lg object-cover"
                            />
                            <span>{p.firstName} {p.lastName}</span>
                          </td>
                          <td className="p-4">{p.serviceType}</td>
                          <td className="p-4">{p.municipality}</td>
                          <td className="p-4 font-mono">{p.phone}</td>
                          <td className="p-4">
                            {p.isApproved === 1 ? (
                              <span className="text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded text-xs">نشط</span>
                            ) : p.isApproved === 0 ? (
                              <span className="text-amber-500 bg-amber-50 px-2 py-0.5 rounded text-xs">معلق</span>
                            ) : (
                              <span className="text-rose-600 bg-rose-50 px-2 py-0.5 rounded text-xs">مرفوض</span>
                            )}
                          </td>
                          <td className="p-4 text-xs font-mono text-slate-400">
                            👁️ {p.impressionsCount} | 📞 {p.visitsCount}
                          </td>
                          <td className="p-4">
                            <div className="flex items-center justify-center gap-1.5">
                              {p.isApproved !== 1 && (
                                <button 
                                  onClick={() => handleApproveProvider(p.id, 1)}
                                  className="p-1.5 text-emerald-600 hover:bg-emerald-50 rounded"
                                  title="تفعيل وقبول"
                                >
                                  <CheckCircle size={16} />
                                </button>
                              )}
                              <button 
                                onClick={() => handleDeleteProvider(p.id)}
                                className="p-1.5 text-rose-600 hover:bg-rose-50 rounded"
                                title="حذف نهائي"
                              >
                                <Trash2 size={16} />
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* ========================================== */}
            {/* SUB-VIEW C: CATEGORIES & MUNICIPALITIES MANAGEMENT */}
            {/* ========================================== */}
            {adminActiveSubTab === 'categories' && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                
                {/* Municipalities Creator list */}
                <div className="bg-white border border-slate-100 rounded-3xl p-6 space-y-5">
                  <h4 className="font-black text-slate-900 border-b border-slate-50 pb-3">إدارة بلديات ولاية وهران</h4>
                  
                  <form onSubmit={handleAddMuni} className="flex gap-2">
                    <input 
                      type="text" 
                      required
                      placeholder="اسم البلدية بالعربية (مثال: أرزيو)"
                      value={newMuniName}
                      onChange={(e) => setNewMuniName(e.target.value)}
                      className="flex-1 px-4 py-2.5 rounded-xl border border-slate-100 text-xs font-bold"
                    />
                    <button 
                      type="submit"
                      className="px-4 py-2.5 bg-primary-600 hover:bg-primary-700 text-white rounded-xl font-bold text-xs flex items-center gap-1 transition-all"
                    >
                      <Plus size={14} />
                      <span>إضافة</span>
                    </button>
                  </form>

                  <div className="grid grid-cols-2 gap-2 max-h-[250px] overflow-y-auto pr-2">
                    {municipalities.map(m => (
                      <div key={m.id} className="p-3 bg-slate-50 rounded-xl border border-slate-100 flex items-center justify-between text-xs font-bold text-slate-700">
                        <span>📍 {m.nameAr}</span>
                        <button 
                          onClick={() => db.deleteMunicipality(m.id).then(() => setMunicipalities(prev => prev.filter(item => item.id !== m.id)))}
                          className="text-slate-400 hover:text-rose-600 transition-colors"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Categories Creator list */}
                <div className="bg-white border border-slate-100 rounded-3xl p-6 space-y-5">
                  <h4 className="font-black text-slate-900 border-b border-slate-50 pb-3">إدارة تصنيفات الخدمات والرموز التعبيرية</h4>

                  <form onSubmit={handleAddCat} className="space-y-3">
                    <div className="grid grid-cols-3 gap-2">
                      <input 
                        type="text" 
                        required
                        placeholder="اسم الخدمة (مثال: حداد)"
                        value={newCatName}
                        onChange={(e) => setNewCatName(e.target.value)}
                        className="col-span-2 px-4 py-2.5 rounded-xl border border-slate-100 text-xs font-bold"
                      />
                      <input 
                        type="text" 
                        required
                        placeholder="رمز (🛠️)"
                        value={newCatIcon}
                        onChange={(e) => setNewCatIcon(e.target.value)}
                        className="px-4 py-2.5 rounded-xl border border-slate-100 text-center text-xs font-bold"
                      />
                    </div>
                    <button 
                      type="submit"
                      className="w-full py-2.5 bg-primary-600 hover:bg-primary-700 text-white rounded-xl font-bold text-xs flex items-center justify-center gap-1 transition-all"
                    >
                      <Plus size={14} />
                      <span>إضافة تصنيف جديد</span>
                    </button>
                  </form>

                  <div className="space-y-2 max-h-[200px] overflow-y-auto pr-2">
                    {categories.map(cat => (
                      <div key={cat.id} className="p-3 bg-slate-50 rounded-xl border border-slate-100 flex items-center justify-between text-xs font-bold text-slate-700">
                        <div className="flex items-center gap-2">
                          <span className="text-lg">{cat.iconName}</span>
                          <span>{cat.nameAr}</span>
                        </div>
                        <button 
                          onClick={() => db.deleteCategory(cat.id).then(() => setCategories(prev => prev.filter(item => item.id !== cat.id)))}
                          className="text-slate-400 hover:text-rose-600 transition-colors"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>

              </div>
            )}

            {/* ========================================== */}
            {/* SUB-VIEW D: REVIEWS MODERATION */}
            {/* ========================================== */}
            {adminActiveSubTab === 'reviews' && (
              <div className="space-y-4">
                <h4 className="text-base font-black text-slate-900">تقييمات ومراجعات الزبائن المكتوبة</h4>

                {reviews.length === 0 ? (
                  <p className="p-8 text-center text-slate-400 bg-white border rounded-3xl text-xs font-bold">لا توجد تقييمات منشورة لمراقبتها في الوقت الحالي.</p>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {reviews.map(rev => {
                      const provider = providers.find(p => p.id === rev.providerId)
                      return (
                        <div key={rev.id} className="bg-white border border-slate-100 p-5 rounded-3xl flex flex-col justify-between gap-4">
                          <div className="space-y-2 text-xs">
                            <div className="flex items-center justify-between">
                              <span className="font-extrabold text-slate-900">المرسل: {rev.reviewerName}</span>
                              <div className="flex items-center gap-0.5 text-amber-500">
                                {[...Array(rev.rating)].map((_, i) => (
                                  <Star key={i} size={11} className="fill-amber-500" />
                                ))}
                              </div>
                            </div>
                            {provider && (
                              <p className="text-[10px] text-primary-600 font-bold">
                                🔗 للحرفي: {provider.firstName} {provider.lastName} ({provider.serviceType})
                              </p>
                            )}
                            <p className="text-slate-500 font-semibold leading-relaxed">&quot;{rev.comment}&quot;</p>
                          </div>

                          <div className="flex items-center justify-between border-t border-slate-50 pt-3">
                            <span className="text-[10px] text-slate-400 font-semibold">
                              {new Date(rev.timestamp).toLocaleString('ar-DZ')}
                            </span>
                            <button
                              onClick={() => handleDeleteReview(rev.id)}
                              className="text-rose-600 hover:text-rose-700 hover:bg-rose-50 p-2 rounded-lg font-bold text-xs flex items-center gap-1 transition-all"
                            >
                              <Trash2 size={13} />
                              <span>حذف</span>
                            </button>
                          </div>
                        </div>
                      )
                    })}
                  </div>
                )}
              </div>
            )}

          </div>
        )}

      </main>

      {/* Decorative Brand footer */}
      <footer className="bg-slate-900 text-slate-400 py-10 border-t border-slate-800 text-center text-xs sm:text-sm font-semibold mt-auto">
        <div className="max-w-7xl mx-auto px-4 space-y-4">
          <p className="text-white font-black text-base">خدمني وهران 🛠️</p>
          <p className="max-w-md mx-auto text-xs text-slate-500 leading-relaxed">
            منصة تكنولوجية وهرانية غير ربحية تسهل تلاقي الحرفيين المحليين الموهوبين مع الباحثين عن خدمات صيانة منزلية عالية الدقة.
          </p>
          <div className="border-t border-slate-800/60 pt-4 text-[11px] text-slate-600 flex flex-col sm:flex-row items-center justify-between gap-4">
            <span>© جميع الحقوق محفوظة {new Date().getFullYear()} - ولاية وهران الباهية</span>
            <div className="flex items-center gap-4">
              <span className="hover:text-primary-400 cursor-pointer" onClick={() => { setActiveTab('seeker'); setSelectedProvider(null); }}>دليل البحث</span>
              <span>•</span>
              <span className="hover:text-primary-400 cursor-pointer" onClick={() => setActiveTab('provider_auth')}>تسجيل كحرفي</span>
              <span>•</span>
              <span className="hover:text-primary-400 cursor-pointer" onClick={() => setActiveTab('admin_auth')}>إدارة المنصة</span>
            </div>
          </div>
        </div>
      </footer>

    </div>
  )
}
