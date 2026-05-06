import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { 
  Briefcase, 
  Calendar, 
  ExternalLink, 
  Filter, 
  Search,
  Edit,
  Trash2,
  Building,
  Plus,
  Mail
} from 'lucide-react';
import toast from 'react-hot-toast';
import AddApplication from './AddApplication';

const Dashboard = () => {
  const [applications, setApplications] = useState([]);
  const [filteredApplications, setFilteredApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [platformFilter, setPlatformFilter] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);
  const [syncingEmails, setSyncingEmails] = useState(false);

  const statusOptions = [
    { value: '', label: 'All Status' },
    { value: 'APPLIED', label: 'Applied' },
    { value: 'OA', label: 'Online Assessment' },
    { value: 'INTERVIEW', label: 'Interview' },
    { value: 'OFFER', label: 'Offer' },
    { value: 'REJECTED', label: 'Rejected' }
  ];

  const platformOptions = [
    { value: '', label: 'All Platforms' },
    { value: 'LINKEDIN', label: 'LinkedIn' },
    { value: 'NAUKRI', label: 'Naukri' },
    { value: 'INDEED', label: 'Indeed' },
    { value: 'OTHER', label: 'Other' }
  ];

  const getStatusColor = (status) => {
    switch (status) {
      case 'APPLIED': return 'bg-blue-100 text-blue-800';
      case 'OA': return 'bg-yellow-100 text-yellow-800';
      case 'INTERVIEW': return 'bg-purple-100 text-purple-800';
      case 'OFFER': return 'bg-green-100 text-green-800';
      case 'REJECTED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getPlatformColor = (platform) => {
    switch (platform) {
      case 'LINKEDIN': return 'bg-blue-50 text-blue-700';
      case 'NAUKRI': return 'bg-orange-50 text-orange-700';
      case 'INDEED': return 'bg-indigo-50 text-indigo-700';
      case 'OTHER': return 'bg-gray-50 text-gray-700';
      default: return 'bg-gray-50 text-gray-700';
    }
  };

  const fetchApplications = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      if (statusFilter) params.append('status', statusFilter);
      if (platformFilter) params.append('platform', platformFilter);
      
      const response = await fetch(`http://localhost:8082/api/applications?${params}`);
      if (!response.ok) throw new Error('Failed to fetch applications');
      
      const data = await response.json();
      setApplications(data);
      setFilteredApplications(data);
    } catch (error) {
      toast.error('Failed to load applications');
      console.error('Error fetching applications:', error);
    } finally {
      setLoading(false);
    }
  };

  const deleteApplication = async (id) => {
    if (!window.confirm('Are you sure you want to delete this application?')) return;
    
    try {
      const response = await fetch(`http://localhost:8082/api/applications/${id}`, {
        method: 'DELETE',
      });
      
      if (!response.ok) throw new Error('Failed to delete application');
      
      toast.success('Application deleted successfully');
      fetchApplications();
    } catch (error) {
      toast.error('Failed to delete application');
      console.error('Error:', error);
    }
  };

  const handleApplicationAdded = () => {
    fetchApplications();
    setShowAddModal(false);
  };

  const handleGmailSync = async () => {
    try {
      setSyncingEmails(true);
      const response = await fetch('http://localhost:8082/api/gmail/sync', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error('Failed to sync Gmail');
      }
      
      const result = await response.json();
      
      if (result.error) {
        toast.error(result.error);
      } else {
        toast.success(`Gmail sync completed! ${result.newApplications || 0} new applications found.`);
        if (result.newApplications > 0) {
          fetchApplications(); // Refresh the applications list
        }
      }
    } catch (error) {
      toast.error('Failed to sync Gmail emails');
      console.error('Gmail sync error:', error);
    } finally {
      setSyncingEmails(false);
    }
  };

  useEffect(() => {
    fetchApplications();
  }, [statusFilter, platformFilter]);

  useEffect(() => {
    const filtered = applications.filter(app => 
      app.companyName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      app.role.toLowerCase().includes(searchTerm.toLowerCase())
    );
    setFilteredApplications(filtered);
  }, [searchTerm, applications]);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <h1 className="text-3xl font-bold text-gray-900">Job Applications</h1>
          <div className="flex items-center gap-3">
            <button
              onClick={handleGmailSync}
              disabled={syncingEmails}
              className="flex items-center gap-2 bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Mail size={20} />
              {syncingEmails ? 'Syncing...' : 'Sync Gmail'}
            </button>
            <button
              onClick={() => setShowAddModal(true)}
              className="flex items-center gap-2 bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors"
            >
              <Plus size={20} />
              Add Application
            </button>
          </div>
        </div>

        {/* Filters */}
        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <div className="flex items-center space-x-2 mb-4">
            <Filter className="h-5 w-5 text-gray-500" />
            <h2 className="text-lg font-semibold text-gray-900">Filters</h2>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search by company or role..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
            </div>
            
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            >
              {statusOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            
            <select
              value={platformFilter}
              onChange={(e) => setPlatformFilter(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            >
              {platformOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Applications List */}
        {filteredApplications.length === 0 ? (
          <div className="bg-white p-12 rounded-lg shadow-sm border border-gray-200 text-center">
            <Briefcase className="h-16 w-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">No applications found</h3>
            <p className="text-gray-600 mb-6">
              {searchTerm || statusFilter || platformFilter 
                ? 'Try adjusting your filters or search terms' 
                : 'Start tracking your job applications by adding your first one'}
            </p>
            <a
              href="/add-application"
              className="inline-flex items-center px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
            >
              Add Your First Application
            </a>
          </div>
        ) : (
          <div className="grid gap-4">
            {filteredApplications.map((application) => (
              <div
                key={application.id}
                className="bg-white p-6 rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow"
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <div className="flex items-center space-x-3 mb-2">
                      <Building className="h-5 w-5 text-gray-500" />
                      <h3 className="text-lg font-semibold text-gray-900">
                        {application.companyName}
                      </h3>
                      <span className={`px-2 py-1 text-xs font-medium rounded-full ${getPlatformColor(application.platform)}`}>
                        {application.platform}
                      </span>
                    </div>
                    
                    <div className="flex items-center space-x-2 text-gray-600 mb-3">
                      <Briefcase className="h-4 w-4" />
                      <span className="font-medium">{application.role}</span>
                      <span className={`px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(application.status)}`}>
                        {application.status}
                      </span>
                    </div>
                    
                    <div className="flex items-center space-x-4 text-sm text-gray-500">
                      <div className="flex items-center space-x-1">
                        <Calendar className="h-4 w-4" />
                        <span>Applied {format(new Date(application.appliedDate), 'MMM dd, yyyy')}</span>
                      </div>
                      <span className="text-gray-400">•</span>
                      <span className="text-gray-500">
                        Source: {application.source === 'MANUAL' ? 'Manual' : 
                                application.source === 'EMAIL' ? 'Email' : 'Extension'}
                      </span>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-2">
                    {application.jobLink && (
                      <a
                        href={application.jobLink}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="p-2 text-gray-500 hover:text-primary-600 transition-colors"
                        title="View Job Posting"
                      >
                        <ExternalLink className="h-4 w-4" />
                      </a>
                    )}
                    <button
                      className="p-2 text-gray-500 hover:text-blue-600 transition-colors"
                      title="Edit Application"
                    >
                      <Edit className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => deleteApplication(application.id)}
                      className="p-2 text-gray-500 hover:text-red-600 transition-colors"
                      title="Delete Application"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>
                
                {application.notes && (
                  <div className="mt-3 pt-3 border-t border-gray-100">
                    <p className="text-sm text-gray-600">{application.notes}</p>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
      
      {/* Add Application Modal */}
      {showAddModal && (
        <AddApplication
          onClose={() => setShowAddModal(false)}
          onApplicationAdded={handleApplicationAdded}
        />
      )}
    </>
  );
};

export default Dashboard;
