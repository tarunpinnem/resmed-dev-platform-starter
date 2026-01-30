import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { patientApi, healthApi } from '../api/client';
import {
  Users,
  UserPlus,
  Activity,
  CheckCircle,
  XCircle,
  Clock,
} from 'lucide-react';
import { format } from 'date-fns';

export default function DashboardPage() {
  const { data: patientsData } = useQuery({
    queryKey: ['patients', 'dashboard'],
    queryFn: () => patientApi.getAll(0, 5),
  });

  const { data: healthData } = useQuery({
    queryKey: ['health'],
    queryFn: healthApi.getHealth,
    refetchInterval: 30000,
  });

  const { data: readyData } = useQuery({
    queryKey: ['ready'],
    queryFn: healthApi.getReady,
    refetchInterval: 30000,
  });

  const stats = [
    {
      name: 'Total Patients',
      value: patientsData?.totalElements ?? 0,
      icon: Users,
      color: 'bg-primary-500',
    },
    {
      name: 'Active Patients',
      value: patientsData?.content.filter(p => p.status === 'ACTIVE').length ?? 0,
      icon: UserPlus,
      color: 'bg-healthcare-500',
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600">Welcome to the Healthcare Platform</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((stat) => (
          <div key={stat.name} className="card p-6">
            <div className="flex items-center gap-4">
              <div className={`p-3 rounded-lg ${stat.color}`}>
                <stat.icon className="h-6 w-6 text-white" />
              </div>
              <div>
                <p className="text-sm text-gray-600">{stat.name}</p>
                <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
              </div>
            </div>
          </div>
        ))}

        {/* Health Status */}
        <div className="card p-6">
          <div className="flex items-center gap-4">
            <div className={`p-3 rounded-lg ${healthData?.status === 'UP' ? 'bg-healthcare-500' : 'bg-red-500'}`}>
              <Activity className="h-6 w-6 text-white" />
            </div>
            <div>
              <p className="text-sm text-gray-600">API Status</p>
              <div className="flex items-center gap-2">
                {healthData?.status === 'UP' ? (
                  <CheckCircle className="h-5 w-5 text-healthcare-500" />
                ) : (
                  <XCircle className="h-5 w-5 text-red-500" />
                )}
                <span className="text-lg font-semibold">{healthData?.status ?? 'Unknown'}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Database Status */}
        <div className="card p-6">
          <div className="flex items-center gap-4">
            <div className={`p-3 rounded-lg ${readyData?.checks?.database === 'UP' ? 'bg-healthcare-500' : 'bg-red-500'}`}>
              <CheckCircle className="h-6 w-6 text-white" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Database</p>
              <div className="flex items-center gap-2">
                {readyData?.checks?.database === 'UP' ? (
                  <CheckCircle className="h-5 w-5 text-healthcare-500" />
                ) : (
                  <XCircle className="h-5 w-5 text-red-500" />
                )}
                <span className="text-lg font-semibold">{readyData?.checks?.database ?? 'Unknown'}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Patients */}
      <div className="card">
        <div className="p-6 border-b">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">Recent Patients</h2>
            <Link to="/patients" className="text-sm text-primary-600 hover:text-primary-700">
              View all
            </Link>
          </div>
        </div>
        <div className="divide-y">
          {patientsData?.content.length === 0 ? (
            <div className="p-6 text-center text-gray-500">
              No patients found. <Link to="/patients/new" className="text-primary-600">Add your first patient</Link>
            </div>
          ) : (
            patientsData?.content.map((patient) => (
              <Link
                key={patient.id}
                to={`/patients/${patient.id}`}
                className="flex items-center justify-between p-4 hover:bg-gray-50 transition-colors"
              >
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 rounded-full bg-primary-100 flex items-center justify-center">
                    <span className="text-sm font-medium text-primary-700">
                      {patient.firstName[0]}{patient.lastName[0]}
                    </span>
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">
                      {patient.firstName} {patient.lastName}
                    </p>
                    <p className="text-sm text-gray-500">{patient.medicalRecordNumber}</p>
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  <span
                    className={`px-2 py-1 text-xs font-medium rounded-full ${
                      patient.status === 'ACTIVE'
                        ? 'bg-healthcare-100 text-healthcare-700'
                        : 'bg-gray-100 text-gray-700'
                    }`}
                  >
                    {patient.status}
                  </span>
                  <div className="flex items-center gap-1 text-sm text-gray-500">
                    <Clock className="h-4 w-4" />
                    {format(new Date(patient.createdAt), 'MMM d, yyyy')}
                  </div>
                </div>
              </Link>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
