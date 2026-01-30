import { useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { patientApi } from '../api/client';
import type { PatientRequest } from '../types';
import { ArrowLeft, Save } from 'lucide-react';

export default function PatientFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isEditing = !!id;

  const { data: patient, isLoading: isLoadingPatient } = useQuery({
    queryKey: ['patient', id],
    queryFn: () => patientApi.getById(id!),
    enabled: isEditing,
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<PatientRequest>();

  useEffect(() => {
    if (patient) {
      reset({
        firstName: patient.firstName,
        lastName: patient.lastName,
        dateOfBirth: patient.dateOfBirth,
        email: patient.email || '',
        phone: patient.phone || '',
        address: patient.address || '',
      });
    }
  }, [patient, reset]);

  const createMutation = useMutation({
    mutationFn: patientApi.create,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['patients'] });
      navigate(`/patients/${data.id}`);
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: PatientRequest) => patientApi.update(id!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patients'] });
      queryClient.invalidateQueries({ queryKey: ['patient', id] });
      navigate(`/patients/${id}`);
    },
  });

  const onSubmit = (data: PatientRequest) => {
    // Clean up empty strings
    const cleanedData: PatientRequest = {
      ...data,
      email: data.email || undefined,
      phone: data.phone || undefined,
      address: data.address || undefined,
    };

    if (isEditing) {
      updateMutation.mutate(cleanedData);
    } else {
      createMutation.mutate(cleanedData);
    }
  };

  if (isEditing && isLoadingPatient) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  const mutation = isEditing ? updateMutation : createMutation;

  return (
    <div className="max-w-2xl mx-auto">
      <div className="flex items-center gap-4 mb-6">
        <Link
          to={isEditing ? `/patients/${id}` : '/patients'}
          className="p-2 hover:bg-gray-100 rounded-md transition-colors"
        >
          <ArrowLeft className="h-5 w-5 text-gray-500" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {isEditing ? 'Edit Patient' : 'New Patient'}
          </h1>
          <p className="text-gray-600">
            {isEditing ? 'Update patient information' : 'Add a new patient to the system'}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="card p-6 space-y-6">
        {mutation.error && (
          <div className="p-3 rounded-md bg-red-50 text-red-700 text-sm">
            An error occurred. Please try again.
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label htmlFor="firstName" className="label">
              First Name <span className="text-red-500">*</span>
            </label>
            <input
              {...register('firstName', { required: 'First name is required' })}
              type="text"
              id="firstName"
              className="input"
              placeholder="John"
            />
            {errors.firstName && (
              <p className="mt-1 text-sm text-red-600">{errors.firstName.message}</p>
            )}
          </div>

          <div>
            <label htmlFor="lastName" className="label">
              Last Name <span className="text-red-500">*</span>
            </label>
            <input
              {...register('lastName', { required: 'Last name is required' })}
              type="text"
              id="lastName"
              className="input"
              placeholder="Doe"
            />
            {errors.lastName && (
              <p className="mt-1 text-sm text-red-600">{errors.lastName.message}</p>
            )}
          </div>
        </div>

        <div>
          <label htmlFor="dateOfBirth" className="label">
            Date of Birth <span className="text-red-500">*</span>
          </label>
          <input
            {...register('dateOfBirth', { required: 'Date of birth is required' })}
            type="date"
            id="dateOfBirth"
            className="input"
          />
          {errors.dateOfBirth && (
            <p className="mt-1 text-sm text-red-600">{errors.dateOfBirth.message}</p>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label htmlFor="email" className="label">
              Email
            </label>
            <input
              {...register('email', {
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Invalid email address',
                },
              })}
              type="email"
              id="email"
              className="input"
              placeholder="john.doe@example.com"
            />
            {errors.email && (
              <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
            )}
          </div>

          <div>
            <label htmlFor="phone" className="label">
              Phone
            </label>
            <input
              {...register('phone')}
              type="tel"
              id="phone"
              className="input"
              placeholder="+14155551234"
            />
            {errors.phone && (
              <p className="mt-1 text-sm text-red-600">{errors.phone.message}</p>
            )}
          </div>
        </div>

        <div>
          <label htmlFor="address" className="label">
            Address
          </label>
          <textarea
            {...register('address')}
            id="address"
            rows={3}
            className="input"
            placeholder="123 Healthcare Ave, Medical City, MC 12345"
          />
        </div>

        <div className="flex justify-end gap-2 pt-4 border-t">
          <Link
            to={isEditing ? `/patients/${id}` : '/patients'}
            className="btn-secondary"
          >
            Cancel
          </Link>
          <button
            type="submit"
            disabled={isSubmitting || mutation.isPending}
            className="btn-primary"
          >
            <Save className="h-4 w-4 mr-2" />
            {mutation.isPending ? 'Saving...' : isEditing ? 'Update Patient' : 'Create Patient'}
          </button>
        </div>
      </form>
    </div>
  );
}
