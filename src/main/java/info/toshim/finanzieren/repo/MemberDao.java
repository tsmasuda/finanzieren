package info.toshim.finanzieren.repo;

import info.toshim.finanzieren.domain.Member;

import java.util.List;

public interface MemberDao
{
	public Member findById(Long id);

	public Member findByEmail(String email);

	public List<Member> findAllOrderedByName();

	public void register(Member member);
}