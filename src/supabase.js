import { createClient } from '@supabase/supabase-js'

// Retrieve credentials safely from Environment variables or fallback to Kotlin configs
const SUPABASE_URL = (import.meta.env.VITE_SUPABASE_URL || 'https://qidegjmauzfctggplgtp.supabase.co').replace(/\/rest\/v1\/?$/, '')
const SUPABASE_ANON_KEY = import.meta.env.VITE_SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFpZGVnam1hdXpmY3RnZ3BsZ3RwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQyMjc1NTIsImV4cCI6MjA5OTgwMzU1Mn0.MWyFesO0xNP88MFEUZwVqb2lXHGFwhFD-DlWKpmPAmE'

export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY)

// --- Premium Seed Data for local fallback and initial presentation ---
const DEFAULT_MUNICIPALITIES = [
  { id: 1, nameAr: 'وهران' },
  { id: 2, nameAr: 'بئر الجير' },
  { id: 3, nameAr: 'السانية' }
]

const DEFAULT_CATEGORIES = [
  { id: 1, nameAr: 'سباك', iconName: '🔧' },
  { id: 2, nameAr: 'كهربائي', iconName: '⚡' },
  { id: 3, nameAr: 'بناء', iconName: '🧱' },
  { id: 4, nameAr: 'دهان', iconName: '🎨' },
  { id: 5, nameAr: 'نجار', iconName: '🪚' },
  { id: 6, nameAr: 'حداد', iconName: '🛠️' },
  { id: 7, nameAr: 'مبلط', iconName: '📐' },
  { id: 8, nameAr: 'عامل تنظيف', iconName: '🧹' },
  { id: 9, nameAr: 'نقل الأثاث', iconName: '🚚' },
  { id: 10, nameAr: 'ميكانيكي', iconName: '🚗' },
  { id: 11, nameAr: 'إصلاح أجهزة', iconName: '🔌' },
  { id: 12, nameAr: 'تركيب المكيفات', iconName: '❄️' },
  { id: 13, nameAr: 'ألمنيوم وزجاج', iconName: '🪟' },
  { id: 14, nameAr: 'بستاني', iconName: '🏡' },
  { id: 15, nameAr: 'خدمات أخرى', iconName: '✨' }
]

const DEFAULT_PROVIDERS = [
  {
    id: 1,
    firstName: 'أحمد',
    lastName: 'بلقاسم',
    municipality: 'وهران',
    address: 'حي الدرب، وهران',
    phone: '0555123456',
    profilePic: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?w=150',
    serviceType: 'سباك',
    shortDescription: 'متخصص في إصلاح جميع أنواع التسريبات وتركيب التمديدات الصحية بخبرة تفوق 10 سنوات.',
    yearsOfExperience: 12,
    municipalities: 'وهران,بئر الجير',
    password: 'password123',
    isApproved: 1, // Approved
    impressionsCount: 145,
    visitsCount: 34,
    joinDate: Date.now() - 30 * 24 * 3600 * 1000
  },
  {
    id: 2,
    firstName: 'سفيان',
    lastName: 'معزوز',
    municipality: 'بئر الجير',
    address: 'حي الياسمين، بئر الجير',
    phone: '0666987654',
    profilePic: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150',
    serviceType: 'كهربائي',
    shortDescription: 'تصليح الأعطال الكهربائية المنزلية وتمديد الشبكات الجديدة بكفاءة وأمان تام.',
    yearsOfExperience: 8,
    municipalities: 'بئر الجير,السانية,وهران',
    password: 'password123',
    isApproved: 1, // Approved
    impressionsCount: 98,
    visitsCount: 21,
    joinDate: Date.now() - 15 * 24 * 3600 * 1000
  },
  {
    id: 3,
    firstName: 'مصطفى',
    lastName: 'حمادي',
    municipality: 'السانية',
    address: 'المنطقة الصناعية، السانية',
    phone: '0777555444',
    profilePic: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150',
    serviceType: 'دهان',
    shortDescription: 'دهان منازل وشقق بأحدث الألوان العصرية ورق الجدران والديكورات الداخلية.',
    yearsOfExperience: 15,
    municipalities: 'السانية,وهران',
    password: 'password123',
    isApproved: 1, // Approved
    impressionsCount: 210,
    visitsCount: 67,
    joinDate: Date.now() - 60 * 24 * 3600 * 1000
  },
  {
    id: 4,
    firstName: 'محمد',
    lastName: 'يوسفي',
    municipality: 'وهران',
    address: 'حي العقيد لطفي، وهران',
    phone: '0550112233',
    profilePic: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150',
    serviceType: 'تركيب المكيفات',
    shortDescription: 'تركيب وصيانة وشحن غاز لجميع أنواع المكيفات الهوائية بأسعار تنافسية وضمان.',
    yearsOfExperience: 6,
    municipalities: 'وهران,بئر الجير,السانية',
    password: 'password123',
    isApproved: 1, // Approved
    impressionsCount: 80,
    visitsCount: 18,
    joinDate: Date.now() - 5 * 24 * 3600 * 1000
  },
  {
    id: 5,
    firstName: 'خالد',
    lastName: 'بوعزة',
    municipality: 'السانية',
    address: 'حي النصر، السانية',
    phone: '0655443322',
    profilePic: 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150',
    serviceType: 'بناء',
    shortDescription: 'بناء وتوسعة وترميم المنازل والخرسانة المسلحة بجودة وإتقان عاليين.',
    yearsOfExperience: 10,
    municipalities: 'السانية',
    password: 'password123',
    isApproved: 0, // Pending Approval
    impressionsCount: 5,
    visitsCount: 1,
    joinDate: Date.now()
  }
]

const DEFAULT_REVIEWS = [
  {
    id: 1,
    providerId: 1,
    reviewerName: 'عمر بن سعيد',
    rating: 5,
    comment: 'عمل متقن جداً وسريع، محترم وملتزم بالموعد. أنصح بالتعامل معه بشدة!',
    timestamp: Date.now() - 10 * 24 * 3600 * 1000
  },
  {
    id: 2,
    providerId: 1,
    reviewerName: 'مراد وهراني',
    rating: 4,
    comment: 'جيد جداً، قام بتصليح تسريب الحمام بنجاح والسعر كان معقولاً.',
    timestamp: Date.now() - 5 * 24 * 3600 * 1000
  },
  {
    id: 3,
    providerId: 2,
    reviewerName: 'حميد بئر الجير',
    rating: 5,
    comment: 'كهربائي ممتاز ومتمكن، حل مشكلة انقطاع الكهرباء المتكرر في دقائق معدودة.',
    timestamp: Date.now() - 3 * 24 * 3600 * 1000
  },
  {
    id: 4,
    providerId: 3,
    reviewerName: 'فاطمة الزهراء',
    rating: 5,
    comment: 'ما شاء الله دهان محترف، عمل نظيف وخامات ممتازة، تعامله راقي جداً.',
    timestamp: Date.now() - 25 * 24 * 3600 * 1000
  },
  {
    id: 5,
    providerId: 3,
    reviewerName: 'سيد أحمد',
    rating: 5,
    comment: 'إتقان في العمل والتزام تام بالوقت المبرم. بارك الله فيه.',
    timestamp: Date.now() - 12 * 24 * 3600 * 1000
  }
]

const DEFAULT_USERS = [
  {
    id: 1,
    firstName: 'زائر',
    lastName: 'وهراني',
    municipality: 'وهران',
    address: 'وسط المدينة، وهران',
    phone: '0555000000',
    password: 'user123',
    role: 'seeker'
  }
]

const DEFAULT_ADMINS = [
  {
    id: 1,
    username: 'admin',
    password: 'admin'
  }
]

// --- Helper Local Storage Initializer ---
const initLocalStorage = () => {
  if (!localStorage.getItem('muni_loaded')) {
    localStorage.setItem('municipalities', JSON.stringify(DEFAULT_MUNICIPALITIES))
    localStorage.setItem('muni_loaded', 'true')
  }
  if (!localStorage.getItem('cats_loaded')) {
    localStorage.setItem('service_categories', JSON.stringify(DEFAULT_CATEGORIES))
    localStorage.setItem('cats_loaded', 'true')
  }
  if (!localStorage.getItem('provs_loaded')) {
    localStorage.setItem('service_providers', JSON.stringify(DEFAULT_PROVIDERS))
    localStorage.setItem('provs_loaded', 'true')
  }
  if (!localStorage.getItem('revs_loaded')) {
    localStorage.setItem('reviews', JSON.stringify(DEFAULT_REVIEWS))
    localStorage.setItem('revs_loaded', 'true')
  }
  if (!localStorage.getItem('users_loaded')) {
    localStorage.setItem('users', JSON.stringify(DEFAULT_USERS))
    localStorage.setItem('users_loaded', 'true')
  }
  if (!localStorage.getItem('admins_loaded')) {
    localStorage.setItem('admin_users', JSON.stringify(DEFAULT_ADMINS))
    localStorage.setItem('admins_loaded', 'true')
  }
}

initLocalStorage()

// Local getters/setters helper
const getLocal = (key) => JSON.parse(localStorage.getItem(key) || '[]')
const setLocal = (key, data) => localStorage.setItem(key, JSON.stringify(data))

// --- Database Unified Interface (Supabase with Local Fallback) ---
export const db = {
  // --- Sync local storage state with Supabase cloud on-demand ---
  async syncWithSupabase() {
    try {
      console.log('Synchronizing with Supabase cloud database...')

      // 1. Fetch Municipalities
      const { data: mData, error: mErr } = await supabase.from('municipalities').select('*')
      if (!mErr && mData && mData.length > 0) {
        setLocal('municipalities', mData.map(d => ({ id: d.id, nameAr: d.name_ar })))
      }

      // 2. Fetch Categories
      const { data: cData, error: cErr } = await supabase.from('service_categories').select('*')
      if (!cErr && cData && cData.length > 0) {
        setLocal('service_categories', cData.map(d => ({ id: d.id, nameAr: d.name_ar, iconName: d.icon_name })))
      }

      // 3. Fetch Service Providers
      const { data: pData, error: pErr } = await supabase.from('service_providers').select('*')
      if (!pErr && pData && pData.length > 0) {
        setLocal('service_providers', pData.map(d => ({
          id: d.id,
          firstName: d.first_name,
          lastName: d.last_name,
          municipality: d.municipality,
          address: d.address,
          phone: d.phone,
          profilePic: d.profile_pic,
          serviceType: d.service_type,
          shortDescription: d.short_description,
          yearsOfExperience: d.years_of_experience,
          municipalities: d.municipalities,
          password: d.password,
          isApproved: d.is_approved,
          impressionsCount: d.impressions_count || 0,
          visitsCount: d.visits_count || 0,
          joinDate: parseInt(d.join_date) || Date.now()
        })))
      }

      // 4. Fetch Reviews
      const { data: rData, error: rErr } = await supabase.from('reviews').select('*')
      if (!rErr && rData && rData.length > 0) {
        setLocal('reviews', rData.map(d => ({
          id: d.id,
          providerId: d.provider_id,
          reviewerName: d.reviewer_name,
          rating: d.rating,
          comment: d.comment,
          timestamp: parseInt(d.timestamp) || Date.now()
        })))
      }

      // 5. Fetch Users
      const { data: uData, error: uErr } = await supabase.from('users').select('*')
      if (!uErr && uData && uData.length > 0) {
        setLocal('users', uData.map(d => ({
          id: d.id,
          firstName: d.first_name,
          lastName: d.last_name,
          municipality: d.municipality,
          address: d.address,
          phone: d.phone,
          password: d.password,
          role: d.role
        })))
      }

      // 6. Fetch Admin Users
      const { data: aData, error: aErr } = await supabase.from('admin_users').select('*')
      if (!aErr && aData && aData.length > 0) {
        setLocal('admin_users', aData.map(d => ({
          id: d.id,
          username: d.username,
          password: d.password
        })))
      }

      return { success: true, message: 'تم تحديث البيانات بنجاح من قاعدة البيانات السحابية!' }
    } catch (err) {
      console.warn('Sync failed, using cached offline data:', err)
      return { success: false, error: err.message || err }
    }
  },

  // --- MUNICIPALITIES ---
  async getMunicipalities() {
    try {
      const { data, error } = await supabase.from('municipalities').select('*')
      if (error) throw error
      const list = data.map(d => ({ id: d.id, nameAr: d.name_ar }))
      setLocal('municipalities', list)
      return list
    } catch (e) {
      console.log('Supabase offline: fallback to local municipalities')
      return getLocal('municipalities')
    }
  },

  async addMunicipality(nameAr) {
    const list = getLocal('municipalities')
    const newId = list.length > 0 ? Math.max(...list.map(i => i.id)) + 1 : 1
    const newMuni = { id: newId, nameAr }
    list.push(newMuni)
    setLocal('municipalities', list)

    try {
      await supabase.from('municipalities').insert([{ name_ar: nameAr }])
    } catch (e) {
      console.warn('Supabase offline insertion')
    }
    return newMuni
  },

  async deleteMunicipality(id) {
    let list = getLocal('municipalities')
    const item = list.find(i => i.id === id)
    list = list.filter(i => i.id !== id)
    setLocal('municipalities', list)

    try {
      if (item) {
        await supabase.from('municipalities').delete().eq('name_ar', item.nameAr)
      }
    } catch (e) {
      console.warn('Supabase offline delete')
    }
  },

  // --- CATEGORIES ---
  async getCategories() {
    try {
      const { data, error } = await supabase.from('service_categories').select('*')
      if (error) throw error
      const list = data.map(d => ({ id: d.id, nameAr: d.name_ar, iconName: d.icon_name }))
      setLocal('service_categories', list)
      return list
    } catch (e) {
      return getLocal('service_categories')
    }
  },

  async addCategory(nameAr, iconName) {
    const list = getLocal('service_categories')
    const newId = list.length > 0 ? Math.max(...list.map(i => i.id)) + 1 : 1
    const newCat = { id: newId, nameAr, iconName }
    list.push(newCat)
    setLocal('service_categories', list)

    try {
      await supabase.from('service_categories').insert([{ name_ar: nameAr, icon_name: iconName }])
    } catch (e) {
      console.warn('Supabase offline insertion')
    }
    return newCat
  },

  async deleteCategory(id) {
    let list = getLocal('service_categories')
    const item = list.find(i => i.id === id)
    list = list.filter(i => i.id !== id)
    setLocal('service_categories', list)

    try {
      if (item) {
        await supabase.from('service_categories').delete().eq('name_ar', item.nameAr)
      }
    } catch (e) {
      console.warn('Supabase offline delete')
    }
  },

  // --- SERVICE PROVIDERS ---
  async getProviders() {
    try {
      const { data, error } = await supabase.from('service_providers').select('*')
      if (error) throw error
      const list = data.map(d => ({
        id: d.id,
        firstName: d.first_name,
        lastName: d.last_name,
        municipality: d.municipality,
        address: d.address,
        phone: d.phone,
        profilePic: d.profile_pic,
        serviceType: d.service_type,
        shortDescription: d.short_description,
        yearsOfExperience: d.years_of_experience,
        municipalities: d.municipalities,
        password: d.password,
        isApproved: d.is_approved,
        impressionsCount: d.impressions_count || 0,
        visitsCount: d.visits_count || 0,
        joinDate: parseInt(d.join_date) || Date.now()
      }))
      setLocal('service_providers', list)
      return list
    } catch (e) {
      return getLocal('service_providers')
    }
  },

  async registerProvider(provider) {
    const list = getLocal('service_providers')
    const newId = list.length > 0 ? Math.max(...list.map(i => i.id)) + 1 : 1
    const newProvider = {
      ...provider,
      id: newId,
      isApproved: 0, // Pending initially
      impressionsCount: 0,
      visitsCount: 0,
      joinDate: Date.now()
    }
    list.push(newProvider)
    setLocal('service_providers', list)

    try {
      await supabase.from('service_providers').insert([{
        id: newId,
        first_name: provider.firstName,
        last_name: provider.lastName,
        municipality: provider.municipality,
        address: provider.address,
        phone: provider.phone,
        profile_pic: provider.profilePic || '',
        service_type: provider.serviceType,
        short_description: provider.shortDescription,
        years_of_experience: parseInt(provider.yearsOfExperience) || 1,
        municipalities: provider.municipalities,
        password: provider.password,
        is_approved: 0,
        impressions_count: 0,
        visits_count: 0,
        join_date: Date.now()
      }])
    } catch (e) {
      console.warn('Supabase offline provider registration')
    }
    return newProvider
  },

  async updateProvider(id, updates) {
    const list = getLocal('service_providers')
    const idx = list.findIndex(i => i.id === id)
    if (idx !== -1) {
      list[idx] = { ...list[idx], ...updates }
      setLocal('service_providers', list)
    }

    try {
      const supabaseUpdates = {}
      if (updates.firstName !== undefined) supabaseUpdates.first_name = updates.firstName
      if (updates.lastName !== undefined) supabaseUpdates.lastName = updates.lastName
      if (updates.municipality !== undefined) supabaseUpdates.municipality = updates.municipality
      if (updates.address !== undefined) supabaseUpdates.address = updates.address
      if (updates.phone !== undefined) supabaseUpdates.phone = updates.phone
      if (updates.shortDescription !== undefined) supabaseUpdates.short_description = updates.shortDescription
      if (updates.yearsOfExperience !== undefined) supabaseUpdates.years_of_experience = parseInt(updates.yearsOfExperience)
      if (updates.municipalities !== undefined) supabaseUpdates.municipalities = updates.municipalities
      if (updates.isApproved !== undefined) supabaseUpdates.is_approved = updates.isApproved
      if (updates.visitsCount !== undefined) supabaseUpdates.visits_count = updates.visitsCount
      if (updates.impressionsCount !== undefined) supabaseUpdates.impressions_count = updates.impressionsCount

      await supabase.from('service_providers').update(supabaseUpdates).eq('id', id)
    } catch (e) {
      console.warn('Supabase offline update')
    }
  },

  async deleteProvider(id) {
    let list = getLocal('service_providers')
    list = list.filter(i => i.id !== id)
    setLocal('service_providers', list)

    try {
      await supabase.from('service_providers').delete().eq('id', id)
    } catch (e) {
      console.warn('Supabase offline delete')
    }
  },

  async approveProvider(id, status) {
    await this.updateProvider(id, { isApproved: status })
  },

  async incrementVisits(id) {
    const list = getLocal('service_providers')
    const p = list.find(i => i.id === id)
    if (p) {
      const newVisits = (p.visitsCount || 0) + 1
      await this.updateProvider(id, { visitsCount: newVisits })
    }
  },

  async incrementImpressions(id) {
    const list = getLocal('service_providers')
    const p = list.find(i => i.id === id)
    if (p) {
      const newImp = (p.impressionsCount || 0) + 1
      await this.updateProvider(id, { impressionsCount: newImp })
    }
  },

  // --- REVIEWS ---
  async getReviews() {
    try {
      const { data, error } = await supabase.from('reviews').select('*')
      if (error) throw error
      const list = data.map(d => ({
        id: d.id,
        providerId: d.provider_id,
        reviewerName: d.reviewer_name,
        rating: d.rating,
        comment: d.comment,
        timestamp: parseInt(d.timestamp) || Date.now()
      }))
      setLocal('reviews', list)
      return list
    } catch (e) {
      return getLocal('reviews')
    }
  },

  async addReview(review) {
    const list = getLocal('reviews')
    const newId = list.length > 0 ? Math.max(...list.map(i => i.id)) + 1 : 1
    const newReview = {
      ...review,
      id: newId,
      timestamp: Date.now()
    }
    list.push(newReview)
    setLocal('reviews', list)

    try {
      await supabase.from('reviews').insert([{
        id: newId,
        provider_id: review.providerId,
        reviewer_name: review.reviewerName,
        rating: review.rating,
        comment: review.comment,
        timestamp: Date.now()
      }])
    } catch (e) {
      console.warn('Supabase offline review creation')
    }
    return newReview
  },

  async deleteReview(id) {
    let list = getLocal('reviews')
    list = list.filter(i => i.id !== id)
    setLocal('reviews', list)

    try {
      await supabase.from('reviews').delete().eq('id', id)
    } catch (e) {
      console.warn('Supabase offline delete')
    }
  },

  // --- USERS (Seekers) ---
  async getUsers() {
    try {
      const { data, error } = await supabase.from('users').select('*')
      if (error) throw error
      const list = data.map(d => ({
        id: d.id,
        firstName: d.first_name,
        lastName: d.last_name,
        municipality: d.municipality,
        address: d.address,
        phone: d.phone,
        password: d.password,
        role: d.role
      }))
      setLocal('users', list)
      return list
    } catch (e) {
      return getLocal('users')
    }
  },

  async registerUser(user) {
    const list = getLocal('users')
    const newId = list.length > 0 ? Math.max(...list.map(i => i.id)) + 1 : 1
    const newUser = {
      ...user,
      id: newId,
      role: 'seeker'
    }
    list.push(newUser)
    setLocal('users', list)

    try {
      await supabase.from('users').insert([{
        id: newId,
        first_name: user.firstName,
        last_name: user.lastName,
        municipality: user.municipality,
        address: user.address,
        phone: user.phone,
        password: user.password,
        role: 'seeker'
      }])
    } catch (e) {
      console.warn('Supabase offline user registration')
    }
    return newUser
  },

  async deleteUser(id) {
    let list = getLocal('users')
    list = list.filter(i => i.id !== id)
    setLocal('users', list)

    try {
      await supabase.from('users').delete().eq('id', id)
    } catch (e) {
      console.warn('Supabase offline delete')
    }
  },

  // --- ADMIN USERS ---
  async getAdminUsers() {
    try {
      const { data, error } = await supabase.from('admin_users').select('*')
      if (error) throw error
      const list = data.map(d => ({
        id: d.id,
        username: d.username,
        password: d.password
      }))
      setLocal('admin_users', list)
      return list
    } catch (e) {
      return getLocal('admin_users')
    }
  }
}
