import { useEffect } from 'react'
import { useMeQuery } from '../api/userApi'
import { useAppDispatch, useAppSelector } from './hooks'
import { userIdSet } from '../features/auth/authSlice'

/** JWTs only carry username + roles; this resolves the numeric userId once per session. */
export function useEnsureUserId() {
  const { token, userId } = useAppSelector((s) => s.auth)
  const dispatch = useAppDispatch()
  const { data } = useMeQuery(undefined, { skip: !token || userId !== null })

  useEffect(() => {
    if (data?.id) dispatch(userIdSet(data.id))
  }, [data, dispatch])

  return userId
}
